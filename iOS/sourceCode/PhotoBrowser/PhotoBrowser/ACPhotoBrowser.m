/**
  * APICloud Modules
  * Copyright (c) 2014-2015 by APICloud, Inc. All Rights Reserved.
  * Licensed under the terms of the The MIT License (MIT).
  * Please see the license.html included with this distribution for details.
  */

#import "ACPhotoBrowser.h"
#import "EBPhotoPagesDataSource.h"
#import "EBPhotoPagesDelegate.h"
#import "EBPhotoPagesController.h"
#import "NSDictionaryUtils.h"
#import "UZAppUtils.h"
#import "UZASIHTTPRequest.h"
#import "EBPhotoViewController.h"
#import <CommonCrypto/CommonDigest.h>
#import <AssetsLibrary/AssetsLibrary.h>
#import <Photos/Photos.h>
#import <objc/message.h>

static int photoid;
@interface ACPhotoBrowser ()
<EBPhotoPagesDataSource, EBPhotoPagesDelegate, ASIHTTPRequestDelegate>
{
    NSInteger openCbid;
    float atime;
    EBPhotoPagesController *_photoPagesController;
     NSMutableArray *_allImages;
  
}

@property (nonatomic, strong) UIImage *placeImage;
@property (nonatomic, strong) NSMutableArray *allImages;
@property (nonatomic, strong) EBPhotoPagesController *photoPagesController;
@property (nonatomic, assign) BOOL isSetImage;
@end

@implementation ACPhotoBrowser

@synthesize placeImage;
@synthesize allImages = _allImages;
@synthesize photoPagesController = _photoPagesController;

#pragma mark - lifeCycle -

- (void)dispose {
    [self close:nil];
}

#pragma mark - interface -

- (void)open:(NSDictionary *)paramsDict_ {

    
    openCbid = [paramsDict_ integerValueForKey:@"cbId" defaultValue:-1];
    atime = [paramsDict_ floatValueForKey:@"atime" defaultValue:0];
    photoid = (int)openCbid;
    if (_photoPagesController) {
        [[_photoPagesController.view superview] bringSubviewToFront:_photoPagesController.view];
        _photoPagesController.view.hidden = NO;
        return;
    }
    //参数读取
    NSArray *allImages = [paramsDict_ arrayValueForKey:@"images" defaultValue:nil];
    if (allImages.count == 0) {
        return;
    }
    BOOL photoBrowserZoomEnable = [paramsDict_ boolValueForKey:@"zoomEnabled" defaultValue:YES];
    NSInteger mode = [paramsDict_ integerValueForKey:@"mode" defaultValue:1];
//    NSInteger scale = [paramsDict_ integerValueForKey:@"scale" defaultValue:0];
    [EBPhotoPagesController setZoomEnable:photoBrowserZoomEnable];
    _allImages = [NSMutableArray arrayWithArray:allImages];
 
    NSInteger activeIndex = [paramsDict_ integerValueForKey:@"activeIndex" defaultValue:0];
    NSString *placeholderImg = [paramsDict_ stringValueForKey:@"placeholderImg" defaultValue:nil];
    if (placeholderImg.length > 0) {
        placeholderImg = [self getPathWithUZSchemeURL:placeholderImg];
        self.placeImage = [UIImage imageWithContentsOfFile:placeholderImg];
    }
    NSString *bgColor = [paramsDict_ stringValueForKey:@"bgColor" defaultValue:@"#000"];
    _photoPagesController = [[EBPhotoPagesController alloc] initWithDataSource:self delegate:self photoAtIndex:activeIndex];
    if (mode == 1) {
        [[NSUserDefaults standardUserDefaults] setObject:@(YES) forKey:@"mode"];
    }else{
        [[NSUserDefaults standardUserDefaults] setObject:@(NO) forKey:@"mode"];
    }
    
    _photoPagesController.backgroundColor = [UZAppUtils colorFromNSString:bgColor];
    
    [[NSUserDefaults standardUserDefaults] setObject:@(atime) forKey:@"atime"];

    [self.viewController addChildViewController:_photoPagesController];
    [self addSubview:_photoPagesController.view fixedOn:nil fixed:YES];
    
    UITapGestureRecognizer *singleRecognizer = [[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(SingleTap:)];
    singleRecognizer.numberOfTapsRequired = 1; // 单击
    [_photoPagesController.view addGestureRecognizer:singleRecognizer];
    
    // 显示回调
    if (_photoPagesController) {
        [self performSelector:@selector(photoPagesControllerShow:) withObject:@(activeIndex) afterDelay:.1];
    }
}
- (void)photoPagesControllerShow:(NSNumber *)activeIndex{
    NSMutableDictionary *sendDict = [NSMutableDictionary dictionary];
    [sendDict setObject:@"show" forKey:@"eventType"];
    [sendDict setObject:[NSNumber numberWithInteger:[activeIndex integerValue]] forKey:@"index"];
    [self sendResultEventWithCallbackId:openCbid dataDict:sendDict errDict:nil doDelete:NO];
}
- (void)close:(NSDictionary *)paramsDict_ {
    if (_photoPagesController) {
        [_photoPagesController.view removeFromSuperview];
        [_photoPagesController removeFromParentViewController];
        _photoPagesController.dataSource = nil;
        _photoPagesController.delegate = nil;
        _photoPagesController.photoPagesDelegate = nil;
        _photoPagesController.photosDataSource = nil;
        _photoPagesController.currentState = nil;
        self.photoPagesController = nil;
    }
    if (_allImages) {
        [_allImages removeAllObjects];
        self.allImages = nil;
    }
    if (placeImage) {
        self.placeImage = nil;
    }
    if(openCbid == photoid){
        photoid = photoid-1;
    }
}

- (void)show:(NSDictionary *)paramsDict_ {
    if (_photoPagesController) {
        [[_photoPagesController.view superview] bringSubviewToFront:_photoPagesController.view];
        _photoPagesController.view.hidden = NO;
    }
    photoid = (int)openCbid;
}

- (void)hide:(NSDictionary *)paramsDict_ {
    if (_photoPagesController) {
        _photoPagesController.view.hidden = YES;
    }
    if(openCbid == photoid){
         photoid = photoid-1;
    }
}

- (void)setIndex:(NSDictionary *)paramsDict_ {
    NSInteger pageIndex = [paramsDict_ integerValueForKey:@"index" defaultValue:0];
    if (_photoPagesController) {
        [_photoPagesController setCurrentIndex:pageIndex];
    }
}

- (void)getIndex:(NSDictionary *)paramsDict_ {
    NSInteger getIndexCbid = [paramsDict_ integerValueForKey:@"cbId" defaultValue:-1];
    NSInteger pageIndex = _photoPagesController.currentPhotoIndex;
    NSMutableDictionary *sendDict = [NSMutableDictionary dictionary];
    [sendDict setObject:[NSNumber numberWithInteger:pageIndex] forKey:@"index"];
    [self sendResultEventWithCallbackId:getIndexCbid dataDict:sendDict errDict:nil doDelete:YES];
}

- (void)getImage:(NSDictionary *)paramsDict_ {
    NSInteger getImgIndex = [paramsDict_ integerValueForKey:@"index" defaultValue:_photoPagesController.currentPhotoIndex];
    if (getImgIndex >= self.allImages.count) {
        return;
    }
    NSString *path = [self.allImages objectAtIndex:getImgIndex];
    if ([path isKindOfClass:[NSString class]] && path.length>0) {
        if ([path hasPrefix:@"http"]) {
            NSString *encodingString = [path stringByAddingPercentEscapesUsingEncoding:NSUTF8StringEncoding];
            NSRange range = [encodingString rangeOfString:@"&"];
            if (range.location != NSNotFound) {
                encodingString = [encodingString stringByReplacingPercentEscapesUsingEncoding:NSUTF8StringEncoding];
            }
            NSString *imageName = [NSString stringWithFormat:@"%@.png",[self md5:encodingString]];
            path = [self getImagePathInCache:imageName];
        } else {
            path = [self getPathWithUZSchemeURL:path];
        }
        NSInteger cbid = [paramsDict_ integerValueForKey:@"cbId" defaultValue:-1];
        [self sendResultEventWithCallbackId:cbid dataDict:[NSDictionary dictionaryWithObject:path forKey:@"path"] errDict:nil doDelete:YES];
    }
}

- (void)setImage:(NSDictionary *)paramsDict_ {
    NSInteger setImgIndex = [paramsDict_ integerValueForKey:@"index" defaultValue:_photoPagesController.currentPhotoIndex];
    if (setImgIndex >= self.allImages.count) {
        return;
    }
    NSString *imagePath = [paramsDict_ stringValueForKey:@"image" defaultValue:@""];
    if (imagePath.length == 0) {
        return;
    }
    [self.allImages setObject:imagePath atIndexedSubscript:setImgIndex];
    if (setImgIndex == _photoPagesController.currentPhotoIndex) {
        if (_photoPagesController) {
            self.isSetImage = YES;
            [_photoPagesController setNoAnimCurrentIndex:setImgIndex];
        }
    }
}

- (void)appendImage:(NSDictionary *)paramsDict_ {
    NSArray *images = [paramsDict_ arrayValueForKey:@"images" defaultValue:@[]];
    if (images.count == 0) {
        return;
    }
    if (_allImages) {
        [_allImages addObjectsFromArray:images];
    }
    if (_photoPagesController) {
        [_photoPagesController setNoAnimCurrentIndex:_photoPagesController.currentPhotoIndex];
    }
}

- (void)deleteImage:(NSDictionary *)paramsDict_ {
    NSInteger dleImgIndex = [paramsDict_ integerValueForKey:@"index" defaultValue:_photoPagesController.currentPhotoIndex];
    if (dleImgIndex >= self.allImages.count) {
        return;
    }
    if (_allImages) {
        [_allImages removeObjectAtIndex:dleImgIndex];
    }
    if (_photoPagesController && dleImgIndex==_photoPagesController.currentPhotoIndex) {
        [_photoPagesController deletePhotoAtIndex:dleImgIndex];
    }
}

- (void)clearCache:(NSDictionary *)paramsDict_ {
    NSString *fullpath = [NSHomeDirectory() stringByAppendingPathComponent:[NSString stringWithFormat:@"Library/Caches/photoBrowser"]];
    NSFileManager *fileManager = [NSFileManager defaultManager];
    [fileManager removeItemAtPath:fullpath error:nil];
}
//- (void)setCookie:(NSDictionary *)paramsDict_ {
//
//    _allCookies = [NSMutableArray array];
//    NSArray *cookies = [paramsDict_ arrayValueForKey:@"properties" defaultValue:@[]];
//    for (NSDictionary *cookie in cookies) {
//        NSString *name = [cookie stringValueForKey:@"name" defaultValue:nil];
//        NSString *value = [cookie stringValueForKey:@"value" defaultValue:nil];
//        NSString *path = [cookie stringValueForKey:@"path" defaultValue:nil];
//        NSString *originURL = [cookie stringValueForKey:@"originURL" defaultValue:nil];
//        NSString *domain = [cookie stringValueForKey:@"domain" defaultValue:nil];
//        NSString *version = [cookie stringValueForKey:@"version" defaultValue:nil];
//        NSString *secure = [cookie stringValueForKey:@"secure" defaultValue:nil];
//        NSString *expires = [cookie stringValueForKey:@"expires" defaultValue:nil];
//        NSString *comment = [cookie stringValueForKey:@"comment" defaultValue:nil];
//        NSString *commentURL = [cookie stringValueForKey:@"commentURL" defaultValue:nil];
//        NSString *discard = [cookie stringValueForKey:@"discard" defaultValue:nil];
//        NSString *maximumAge = [cookie stringValueForKey:@"maximumAge" defaultValue:nil];
//        NSString *port = [cookie stringValueForKey:@"port" defaultValue:nil];
//
//        NSMutableDictionary *properties = [[NSMutableDictionary alloc] init];
//        if (name != nil) {
//            [properties setObject:name forKey:NSHTTPCookieName];
//        }
//        if (value != nil) {
//            [properties setObject:value forKey:NSHTTPCookieValue];
//        }
//        if (path != nil) {
//            [properties setObject:path forKey:NSHTTPCookiePath];
//        }
//        if (originURL != nil) {
//            [properties setObject:originURL forKey:NSHTTPCookieOriginURL];
//        }
//        if (domain != nil) {
//            [properties setObject:domain forKey:NSHTTPCookieDomain];
//        }
//        if (version != nil) {
//            [properties setObject:version forKey:NSHTTPCookieVersion];
//        }
//        if (secure != nil) {
//            [properties setObject:secure forKey:NSHTTPCookieSecure];
//        }
//
//        if (expires != nil) {
//            [properties setObject:[NSDate dateWithTimeIntervalSinceNow:[expires doubleValue]] forKey:NSHTTPCookieExpires];
//        }
//        if (comment != nil) {
//            [properties setObject:comment forKey:NSHTTPCookieComment];
//        }
//        if (commentURL != nil) {
//            [properties setObject:commentURL forKey:NSHTTPCookieCommentURL];
//        }
//        if (discard != nil) {
//            [properties setObject:discard forKey:NSHTTPCookieDiscard];
//        }
//        if (maximumAge != nil) {
//            [properties setObject:maximumAge forKey:NSHTTPCookieMaximumAge];
//        }
//        if (port != nil) {
//            [properties setObject:port forKey:NSHTTPCookiePort];
//        }
//        NSHTTPCookie *cookie = [[NSHTTPCookie alloc] initWithProperties:properties];
//        if (cookie != nil) {
//            [_allCookies addObject:cookie];
//            NSHTTPCookieStorage *cookieStorage = [NSHTTPCookieStorage sharedHTTPCookieStorage];
//            [cookieStorage setCookie:cookie];
//        }
//
//
//
//    }
//
//}
//- (void)clearCookie:(NSDictionary *)paramsDict_ {
//    [UZASIHTTPRequest clearSession];
//    [UZASIHTTPRequest setSessionCookies:nil];
//    NSHTTPCookieStorage *cookieStorage = [NSHTTPCookieStorage sharedHTTPCookieStorage];
//    for (NSHTTPCookie *cookie in cookieStorage.cookies) {
//        [cookieStorage deleteCookie:cookie];
//    }
//
//
//}
#pragma mark - EBPhotoPagesDataSource -

- (BOOL)photoPagesController:(EBPhotoPagesController *)photoPagesController shouldExpectPhotoAtIndex:(NSInteger)index {
    if(index < self.allImages.count){
        return YES;
    }
    return NO;
}

- (void)photoPagesController:(EBPhotoPagesController *)controller imageAtIndex:(NSInteger)index completionHandler:(void (^)(UIImage *, BOOL))handler {
    dispatch_queue_t queue = dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0);
    dispatch_async(queue, ^{
        if (self.placeImage) {
            handler(self.placeImage, YES);
        }
        NSString *photoPath = self.allImages[index];
        UIImage *image = nil;
        if ([photoPath hasPrefix:@"http"]) {
   
            NSURL *url = [NSURL URLWithString:photoPath];
            if (!url) {
                url = [NSURL URLWithString:[photoPath stringByAddingPercentEscapesUsingEncoding:NSUTF8StringEncoding]];
            }
            UIImage *image = [self getImageInCacheWithURLStr:url.absoluteString];
            if (image) {
                handler(image,NO);
            } else {
                dispatch_sync(dispatch_get_main_queue(), ^{
                    __weak ACPhotoBrowser *asyImg = self;
                    __block UZASIHTTPRequest *requestAsy = [UZASIHTTPRequest requestWithURL:url];
                    [requestAsy setValidatesSecureCertificate:NO];
                    NSString *fileName = [NSString stringWithFormat:@"%@.png",[self md5:url.absoluteString]];
                    [requestAsy setDownloadDestinationPath:[self getImagePathInCache:fileName]];
                    [requestAsy setDelegate:self];
                    [requestAsy setTimeOutSeconds:15];
                    [requestAsy setCompletionBlock:^(void){
                        requestAsy.delegate = nil;
                        requestAsy = nil;
                        dispatch_queue_t queue = dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_HIGH, 0ul);
                        dispatch_async(queue, ^{
                            UIImage *image = [asyImg getImageInCacheWithURLStr:url.absoluteString];
                            
                            if (image) {
                                handler(image,NO);
                                NSMutableDictionary *sendDict = [NSMutableDictionary dictionary];
                                [sendDict setObject:@"loadImgSuccess" forKey:@"eventType"];
                                [sendDict setObject:[NSNumber numberWithInteger:index] forKey:@"index"];
                                [self sendResultEventWithCallbackId:openCbid dataDict:sendDict errDict:nil doDelete:NO];
                            } else {
                                [requestAsy cancel];
                                requestAsy.delegate = nil;
                                requestAsy = nil;
                                NSMutableDictionary *sendDict = [NSMutableDictionary dictionary];
                                [sendDict setObject:@"loadImgFail" forKey:@"eventType"];
                                [sendDict setObject:[NSNumber numberWithInteger:index] forKey:@"index"];
                                [self sendResultEventWithCallbackId:openCbid dataDict:sendDict errDict:nil doDelete:NO];
                            }
                        });}];
                    [requestAsy setFailedBlock:^(void) {
                        [requestAsy cancel];
                        requestAsy.delegate = nil;
                        requestAsy = nil;
                        NSMutableDictionary *sendDict = [NSMutableDictionary dictionary];
                        [sendDict setObject:@"loadImgFail" forKey:@"eventType"];
                        [sendDict setObject:[NSNumber numberWithInteger:index] forKey:@"index"];
                        [self sendResultEventWithCallbackId:openCbid dataDict:sendDict errDict:nil doDelete:NO];
                    }];
                    [requestAsy startAsynchronous];
                });
            }
        } else if ([photoPath hasPrefix:@"assets-library://"]) {
            // 是否是本地图片?
            ALAssetsLibrary  *assetLib = [[ALAssetsLibrary alloc] init];
            [assetLib assetForURL:[NSURL URLWithString:photoPath] resultBlock:^(ALAsset *asset) {
                 // 使用asset来获取本地图片
                 ALAssetRepresentation *assetRep = [asset defaultRepresentation];
                 CGImageRef imgRef = [assetRep fullResolutionImage];
                 UIImage *targetImage = [UIImage imageWithCGImage:imgRef
                                                           scale:assetRep.scale
                                                     orientation:(UIImageOrientation)assetRep.orientation];
                if (targetImage) {
                    handler(targetImage,NO);
                }
             } failureBlock:^(NSError *error) {
                 // 访问库文件被拒绝,则直接使用默认图片
             }];
        }else if([photoPath hasPrefix:@"widget://"] ||[photoPath hasPrefix:@"fs://"]){
           
            photoPath = [self getPathWithUZSchemeURL:photoPath];
            image = [UIImage imageWithContentsOfFile:photoPath];
            handler(image,NO);
        }else if([photoPath hasPrefix:@"/var"]){
            image = [UIImage imageWithContentsOfFile:photoPath];
            handler(image,NO);
        }else if([photoPath hasPrefix:@"data:image"]){ //base64
            NSURL *url = [NSURL URLWithString: photoPath];
            NSData *data = [NSData dataWithContentsOfURL: url];
            UIImage *image = [UIImage imageWithData: data];
            handler(image,NO);
        }else {
          
            PHFetchResult<PHAsset *> * fetchResult = [PHAsset fetchAssetsWithLocalIdentifiers:@[photoPath] options:nil];
            if (0 == fetchResult.count) {
                return;
            }
            PHAsset * asset = fetchResult.firstObject;
            [self requestImageForAsset:asset size:CGSizeMake(asset.pixelWidth, asset.pixelHeight) resizeMode:PHImageRequestOptionsResizeModeExact completion:^(UIImage * image) {
                handler(image,NO);
            }];
        }
    });
}

- (void)photoPagesController:(EBPhotoPagesController *)photoPagesController didReceiveLongPress:(UILongPressGestureRecognizer *)recognizer forPhotoAtIndex:(NSInteger)index {
    
    NSMutableDictionary *sendDict = [NSMutableDictionary dictionary];
    [sendDict setObject:@"longPress" forKey:@"eventType"];
    [sendDict setObject:[NSNumber numberWithInteger:index] forKey:@"index"];
    [self sendResultEventWithCallbackId:openCbid dataDict:sendDict errDict:nil doDelete:NO];
}

- (BOOL)photoPagesController:(EBPhotoPagesController *)photoPagesController
shouldAllowDeleteForPhotoAtIndex:(NSInteger)index {
    return YES;
}

- (void)photoPagesController:(EBPhotoPagesController *)photoPagesController
       didDeletePhotoAtIndex:(NSInteger)index {

}

#pragma mark - EBPPhotoPagesDelegate

- (void)photoPagesControllerDidChanged:(EBPhotoPagesController *)photoPagesController withIndex:(NSInteger)pageIndex {
    if (self.isSetImage) {
        self.isSetImage = NO;
        return;
    }
    
    NSMutableDictionary *sendDict = [NSMutableDictionary dictionary];
    [sendDict setObject:@"change" forKey:@"eventType"];
    [sendDict setObject:[NSNumber numberWithInteger:pageIndex] forKey:@"index"];
    [self sendResultEventWithCallbackId:openCbid dataDict:sendDict errDict:nil doDelete:NO];
}

- (void)photoPagesControllerDidClick:(EBPhotoPagesController *)photoPagesController withIndex:(NSInteger)pageIndex {//图片加载出来后的点击事件
    //解决加载两个模块走两次代理方法
    if(photoid == openCbid){
        NSMutableDictionary *sendDict = [NSMutableDictionary dictionary];
        [sendDict setObject:@"click" forKey:@"eventType"];
        [sendDict setObject:[NSNumber numberWithInteger:pageIndex] forKey:@"index"];
        [self sendResultEventWithCallbackId:openCbid dataDict:sendDict errDict:nil doDelete:NO];
    }

    
}

#pragma mark - uitility -

- (void) SingleTap:(UIGestureRecognizer *)sender {//图片未加载出来时候的点击事件

    if(photoid == openCbid){
        NSInteger pageIndex = _photoPagesController.currentPhotoIndex;
        NSMutableDictionary *sendDict = [NSMutableDictionary dictionary];
        [sendDict setObject:@"click" forKey:@"eventType"];
        [sendDict setObject:[NSNumber numberWithInteger:pageIndex] forKey:@"index"];
        [self sendResultEventWithCallbackId:openCbid dataDict:sendDict errDict:nil doDelete:NO];
    }


}

- (UIImage*)getImageInCacheWithURLStr:(NSString *)inURLStr {//根据url获取缓存图片
    UIImage *image = nil;
    NSString *imageName = nil;
    if ((![inURLStr isKindOfClass:[NSString class]]) || ([inURLStr length] == 0)){
        return nil;
    }
    imageName = [NSString stringWithFormat:@"%@.png",[self md5:inURLStr]];
    if ([self imageIsExistInCache:imageName]) {
        image = [UIImage imageWithContentsOfFile:[self getImagePathInCache:imageName]];
    }
    return image;
}

- (NSString *)md5:(NSString *)str {//生成加密后的图片名
    const char *cStr = [str UTF8String];
    unsigned char result[16];
    CC_MD5( cStr, (unsigned)strlen(cStr), result );
    return [NSString stringWithFormat:
            @"%02X%02X%02X%02X%02X%02X%02X%02X%02X%02X%02X%02X%02X%02X%02X%02X",
            result[0], result[1], result[2], result[3],
            result[4], result[5], result[6], result[7],
            result[8], result[9], result[10], result[11],
            result[12], result[13], result[14], result[15]
            ];
    
}

- (BOOL)imageIsExistInCache:(NSString*)inImageName {//根据加密后的图片名判断图片是否下载完成
    NSFileManager *manager = [NSFileManager defaultManager];
    NSString *dir = [NSHomeDirectory() stringByAppendingPathComponent:@"Library/Caches/photoBrowser"];
    if (![manager fileExistsAtPath:dir]) {
        [manager createDirectoryAtPath:dir withIntermediateDirectories:YES attributes:nil error:nil];
    }
    NSString *path = [self getImagePathInCache:inImageName];
    return [manager fileExistsAtPath:path];
}

- (NSString *)getImagePathInCache:(NSString *)inImageName {//根据加密后的图片名获取绝对路径
    NSString *realPath = [NSHomeDirectory() stringByAppendingPathComponent:[NSString stringWithFormat:@"Library/Caches/photoBrowser/%@",inImageName]];
    return realPath;
}
#pragma mark - 获取asset对应的图片
- (void)requestImageForAsset:(PHAsset *)asset size:(CGSize)size resizeMode:(PHImageRequestOptionsResizeMode)resizeMode completion:(void (^)(UIImage *))completion
    {
        PHImageRequestOptions *option = [[PHImageRequestOptions alloc] init];
        /**
         resizeMode：对请求的图像怎样缩放。有三种选择：None，默认加载方式；Fast，尽快地提供接近或稍微大于要求的尺寸；Exact，精准提供要求的尺寸。
         deliveryMode：图像质量。有三种值：Opportunistic，在速度与质量中均衡；HighQualityFormat，不管花费多长时间，提供高质量图像；FastFormat，以最快速度提供好的质量。
         这个属性只有在 synchronous 为 true 时有效。
         */
        option.resizeMode = resizeMode;//控制照片尺寸
        option.deliveryMode = PHImageRequestOptionsDeliveryModeFastFormat;//控制照片质量
        option.synchronous = YES;
        option.networkAccessAllowed = YES;
        
        float scale = [UIScreen mainScreen].scale;
        
        
        //param：targetSize 即你想要的图片尺寸，若想要原尺寸则可输入PHImageManagerMaximumSize
        [[PHCachingImageManager defaultManager] requestImageForAsset:asset targetSize:CGSizeMake(size.width*scale, size.height*scale) contentMode:PHImageContentModeAspectFill options:option resultHandler:^(UIImage * _Nullable image, NSDictionary * _Nullable info) {
            completion(image);
        }];
        
    }
@end

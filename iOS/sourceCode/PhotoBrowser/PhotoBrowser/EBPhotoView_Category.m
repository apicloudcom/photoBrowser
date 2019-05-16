//
//  EBPhotoView+EBPhotoView_Category.m
//  PhotoBrowser
//
//  Created by Answer on 2019/4/23.
//  Copyright © 2019年 ___Turbo___. All rights reserved.
//

#import "EBPhotoView_Category.h"
#import <objc/message.h>

@implementation EBPhotoView (EBPhotoView_Category)
- (void)setImage:(UIImage *)image
{
    
    
    NSAssert(image, @"Image cannot be nil");
    if (self.alpha <= 0) {
        [self.imageView setAlpha:0];
        [UIView animateWithDuration:0.1
                              delay:0
                            options:UIViewAnimationOptionCurveEaseIn
                         animations:^{
                             [self.imageView setAlpha:1];
                         }completion:nil];
    }
    
    [self setContentModeForImageSize:image.size];
    [self.imageView setImage:image];
    NSNumber *atime = [[NSUserDefaults standardUserDefaults] objectForKey:@"atime"];
    [[NSUserDefaults standardUserDefaults] setObject:@(0) forKey:@"atime"];
    if ([atime floatValue] > 0) {
        self.imageView.frame = CGRectMake(self.center.x, self.center.y, 0, 0);
        [self updateOriginRect:[atime floatValue]];
    }
}
- (void)updateOriginRect:(float)atime
{
    CGSize picSize = self.imageView.image.size;
    CGRect originRect = CGRectZero;
    if (picSize.width == 0 || picSize.height == 0) {
        return;
    }
    float scaleX = self.frame.size.width/picSize.width;
    float scaleY = self.frame.size.height/picSize.height;
    if (scaleX > scaleY) {
        float imgViewWidth = picSize.width*scaleY;
        self.maximumZoomScale = self.frame.size.width/imgViewWidth;
        originRect = (CGRect){self.frame.size.width/2-imgViewWidth/2,0,imgViewWidth,self.frame.size.height};
    } else  {
        float imgViewHeight = picSize.height*scaleX;
        self.maximumZoomScale = self.frame.size.height/imgViewHeight;
        originRect = (CGRect){0,self.frame.size.height/2-imgViewHeight/2,self.frame.size.width,imgViewHeight};
        self.zoomScale = 1.0;
    }
    [UIView animateWithDuration:atime animations:^{
        self.imageView.frame = originRect;
    }];
}
- (void)setContentModeForImageSize:(CGSize)size
{
    if(self.adjustsContentModeForImageSize == NO){
        [self.imageView setContentMode:UIViewContentModeScaleAspectFit];
        return;
    }
    
    UIViewContentMode newContentMode;
    if((size.height < self.imageView.bounds.size.height) &&
       (size.width  < self.imageView.bounds.size.width ) ){
        newContentMode = UIViewContentModeCenter;
    } else {
        newContentMode = UIViewContentModeScaleAspectFit;
    }
    
    if(self.imageView.contentMode != newContentMode){
        [self.imageView setContentMode:newContentMode];
    }
}
@end

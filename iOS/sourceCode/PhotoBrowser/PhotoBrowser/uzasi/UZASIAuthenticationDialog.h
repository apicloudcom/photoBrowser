//
//  ASIAuthenticationDialog.h
//  Part of ASIHTTPRequest -> http://allseeing-i.com/ASIHTTPRequest
//
//  Created by Ben Copsey on 21/08/2009.
//  Copyright 2009 All-Seeing Interactive. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <UIKit/UIKit.h>
@class UZASIHTTPRequest;

typedef enum _ASIAuthenticationType {
	ASIStandardAuthenticationType = 0,
    ASIProxyAuthenticationType = 1
} ASIAuthenticationType;

@interface UZASIAutorotatingViewController : UIViewController
@end

@interface UZASIAuthenticationDialog : UZASIAutorotatingViewController <UIActionSheetDelegate, UITableViewDelegate, UITableViewDataSource> {
	UZASIHTTPRequest *request;
	ASIAuthenticationType type;
	UITableView *tableView;
	UIViewController *presentingController;
	BOOL didEnableRotationNotifications;
}
+ (void)presentAuthenticationDialogForRequest:(UZASIHTTPRequest *)request;
+ (void)dismiss;

@property (retain) UZASIHTTPRequest *request;
@property (assign) ASIAuthenticationType type;
@property (assign) BOOL didEnableRotationNotifications;
@property (retain, nonatomic) UIViewController *presentingController;
@end

//
//  UZASIInputStream.h
//  Part of ASIHTTPRequest -> http://allseeing-i.com/ASIHTTPRequest
//
//  Created by Ben Copsey on 10/08/2009.
//  Copyright 2009 All-Seeing Interactive. All rights reserved.
//

#import <Foundation/Foundation.h>

@class UZASIHTTPRequest;

// This is a wrapper for NSInputStream that pretends to be an NSInputStream itself
// Subclassing NSInputStream seems to be tricky, and may involve overriding undocumented methods, so we'll cheat instead.
// It is used by ASIHTTPRequest whenever we have a request body, and handles measuring and throttling the bandwidth used for uploading

@interface UZASIInputStream : NSObject {
	NSInputStream *stream;
	UZASIHTTPRequest *request;
}
+ (id)inputStreamWithFileAtPath:(NSString *)path request:(UZASIHTTPRequest *)request;
+ (id)inputStreamWithData:(NSData *)data request:(UZASIHTTPRequest *)request;

@property (retain, nonatomic) NSInputStream *stream;
@property (assign, nonatomic) UZASIHTTPRequest *request;
@end

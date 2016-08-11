//
//  Created by Eddy Borja.
//  Copyright (c) 2014 Eddy Borja. All rights reserved.
//
/*
 Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 
 The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 
 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */


#import "EBCommentCell.h"

@implementation EBCommentCell

- (id)init
{
    self = [super init];
    if (self) {
        [self initialize];
    }
    return self;
}

- (id)initWithCoder:(NSCoder *)aDecoder
{
    self = [super initWithCoder:aDecoder];
    if (self) {
        [self initialize];
    }
    return self;
}

- (id)initWithFrame:(CGRect)frame
{
    self = [super initWithFrame:frame];
    if (self) {
        [self initialize];
    }
    return self;
}


- (id)initWithStyle:(UITableViewCellStyle)style reuseIdentifier:(NSString *)reuseIdentifier
{
    self = [super initWithStyle:style reuseIdentifier:reuseIdentifier];
    if (self) {
        [self initialize];
    }
    return self;
}

- (void)initialize
{
    [self loadAuthorAvatar];
    [self loadAuthorNameButton];
    [self loadCommentLabel];
    [self loadDateLabel];
}

#pragma mark -

- (void)prepareForReuse
{
    [self.authorNameButton setTitle:nil forState:UIControlStateNormal];
    [self.commentLabel setText:nil];
    [self.dateLabel setText:nil];
}

- (void)loadAuthorAvatar
{
    UIImageView *imageView = [[UIImageView alloc] initWithFrame:CGRectMake(10, 5, 40, 40)];
    [self.imageView setContentMode:UIViewContentModeScaleAspectFill];
    [self addSubview:imageView];
    [self setAuthorAvatar:imageView];
}

- (void)loadAuthorNameButton
{
    UIButton *button = [UIButton buttonWithType:UIButtonTypeCustom];
    
    [button.titleLabel setFont:[UIFont fontWithName:@"HelveticaNeue-Medium" size:12]];
    [button.titleLabel setTextColor:[UIColor colorWithWhite:0.8 alpha:1]];
    [button.titleLabel setShadowColor:[UIColor colorWithWhite:0 alpha:0.5]];
    [button.titleLabel setShadowOffset:CGSizeMake(0, 1)];
    [button.titleLabel setBackgroundColor:[UIColor clearColor]];
    [button.titleLabel setTextAlignment:NSTextAlignmentLeft];
    [button setTitleEdgeInsets:UIEdgeInsetsZero];
    [button setContentEdgeInsets:UIEdgeInsetsZero];
    [button setContentHorizontalAlignment:UIControlContentHorizontalAlignmentLeft];
    [button setAutoresizingMask:UIViewAutoresizingFlexibleWidth];
    
    [button setFrame:CGRectMake(55, 5, 95, 12)];
    [self setAuthorNameButton:button];
    [self addSubview:button];
}

- (void)loadCommentLabel
{
    UILabel *textLabel = [UILabel new];
    [textLabel setBackgroundColor:[UIColor redColor]];
    [textLabel setAutoresizingMask:UIViewAutoresizingFlexibleHeight|UIViewAutoresizingFlexibleWidth];
    [textLabel setFont:[UIFont fontWithName:@"HelveticaNeue-Light" size:16]];
    [textLabel setTextColor:[UIColor whiteColor]];
    [textLabel setShadowColor:[UIColor colorWithWhite:0 alpha:0.5]];
    [textLabel setShadowOffset:CGSizeMake(0, 1)];
    [textLabel setBackgroundColor:[UIColor clearColor]];
    [textLabel setNumberOfLines:10000];
    
    [textLabel setFrame:CGRectMake(55, 18, 255, 25)];
    [self setCommentLabel:textLabel];
    [self addSubview:textLabel];
    
}

- (void)loadDateLabel
{
    UILabel *dateLabel = [UILabel new];
    
    [dateLabel setAutoresizingMask:UIViewAutoresizingFlexibleLeftMargin];
    [dateLabel setFont:[UIFont fontWithName:@"HelveticaNeue-Medium" size:12]];
    [dateLabel setTextColor:[UIColor colorWithWhite:0.8 alpha:1]];
    [dateLabel setShadowColor:[UIColor colorWithWhite:0 alpha:0.5]];
    [dateLabel setShadowOffset:CGSizeMake(0, 1)];
    [dateLabel setBackgroundColor:[UIColor clearColor]];
    [dateLabel setTextAlignment:NSTextAlignmentRight];
    
    [dateLabel setFrame:CGRectMake(170, 5, 140, 12)];
    [self setDateLabel:dateLabel];
    [self addSubview:dateLabel];
}


- (void)setComment:(id<EBPhotoCommentProtocol>)comment
{
    NSAssert([comment conformsToProtocol:@protocol(EBPhotoCommentProtocol)], @"Comment object must conform to EBPhotoCommentProtocol");
    NSAssert(self.authorNameButton, @"Must have an button for an author name.");
    NSAssert(self.authorAvatar, @"Must have an image view for author images.");
    NSAssert(self.commentLabel, @"Must have a label for comment text.");
    NSAssert(self.dateLabel, @"Must have a label for displaying dates.");
    
    if([comment respondsToSelector:@selector(authorName)]){
        [self.authorNameButton setTitle:[comment authorName]
                           forState:UIControlStateNormal];
    }
    
    if([comment respondsToSelector:@selector(authorAvatar)]){
        [self.authorAvatar setImage:[comment authorAvatar]];
        [self.authorAvatar.layer setMasksToBounds:YES];
        [self.authorAvatar.layer setRasterizationScale:[UIScreen mainScreen].scale];
        [self.authorAvatar.layer setShouldRasterize:YES];
        [self.authorAvatar.layer setCornerRadius:5.0];
    }
    
    if([comment respondsToSelector:@selector(attributedCommentCharacter)] &&
       [comment attributedCommentCharacter]){
        [self.commentLabel setAttributedText:[comment attributedCommentCharacter]];
    }
    
    if([comment respondsToSelector:@selector(commentCharacter)]) {
        [self.commentLabel setText:[comment commentCharacter]];
    }
    
    [self resizeTextLabel];
    
    if([comment respondsToSelector:@selector(postDate)]){
        NSDate *postDate = [comment postDate];
        NSDateFormatter *dateFormatter = [self dateFormatter];
        NSString *dateString = [dateFormatter stringFromDate:postDate];
        [self.dateLabel setText:dateString];
    }
    
}

- (void)setHighlightColor:(UIColor *)highlightColor
{
    _highlightColor = highlightColor;
    
    UIView *selectedBackground = [[UIView alloc] initWithFrame:self.frame];
    [selectedBackground setBackgroundColor:self.highlightColor];
    [self setSelectedBackgroundView:selectedBackground];
}

- (void)resizeTextLabel
{
    NSString *textForRow = self.commentLabel.attributedText ?
                                    self.commentLabel.attributedText.string :
                                    self.commentLabel.text;
    
    //Get values from the comment cell itself, as an abstract class perhaps.
    //OR better, from reference cells dequeued from the table
    //http://stackoverflow.com/questions/10239040/dynamic-uilabel-heights-widths-in-uitableviewcell-in-all-orientations
    
    CGRect textViewSize = [textForRow boundingRectWithSize:CGSizeMake(self.commentLabel.frame.size.width, 1000) options:NSStringDrawingUsesLineFragmentOrigin attributes:@{NSFontAttributeName: self.commentLabel.font} context:nil];
    CGRect textLabelFrame = self.commentLabel.frame;
    textLabelFrame.size.height = textViewSize.size.height;
    
    [self.commentLabel setFrame:textLabelFrame];
}

- (void)delete:(id)sender
{
    
    //[self.nextResponder delete:self];
    [[NSNotificationCenter defaultCenter] postNotificationName:@"CellDidRequestSelfDeletion" object:self];
}


- (NSDateFormatter *)dateFormatter
{
    static NSDateFormatter *dateFormatter;
    
    if(dateFormatter == nil){
        dateFormatter = [NSDateFormatter new];
        [dateFormatter setTimeStyle:NSDateFormatterShortStyle];
        [dateFormatter setDateStyle:NSDateFormatterMediumStyle];
    }
    
    return dateFormatter;
}

- (void)setSelected:(BOOL)selected animated:(BOOL)animated
{
    [super setSelected:selected animated:animated];

    // Configure the view for the selected state
}

@end

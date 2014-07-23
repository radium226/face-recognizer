#!/bin/env python2

import subprocess as sp

#def ffmpeg_read(stdin, media_info):
def ffmpeg_read(video, media_info):
    command = [
        "ffmpeg", 
        #"-pix_fmt", "yuv420p",
        #"-probesize", "1000000", 
        #"-analyzeduration", "10000000",
        #"-i", "-", 
        "-i", video, 
        "-f", "image2pipe", 
        "-s", "%dx%d" % (media_info.width, media_info.height), 
        "-pix_fmt", "bgr24", 
        "-vcodec", "rawvideo", 
        "-r", str(media_info.frame_rate / 4),
        "-"
    ]
    #print "ffmpeg_read = %s" % " ".join(command)
    process = sp.Popen(command, stdout = sp.PIPE, stderr = sp.PIPE, bufsize = media_info.width * media_info.height * 3, shell = False)
    return process

def ffmpeg_write(media_info):
    command = [
        "ffmpeg", "-y", 
        "-f", "rawvideo", 
        "-s", "%dx%d" % (media_info.width, media_info.height),
        "-pix_fmt", "bgr24", 
        "-r", str(media_info.frame_rate / 4), 
        "-i", "-", 
        "-an", 
        "-vcodec", "flv",
        "-f", "flv",
        "-s", "%dx%d" % (media_info.width, media_info.height),
        "-"
    ]
    #prihaarcascade_mcs_nose.xmlnt "ffmpeg_write = %s" % " ".join(command)
    #print type(stdin)
    process = sp.Popen(command, stdin = sp.PIPE, stdout = sp.PIPE, stderr = sp.PIPE, shell = False)
    return process

def tee(stdin, path):
    command = ["tee", path]
    process = sp.Popen(command, stdin = stdin, stdout = sp.PIPE, stderr = sp.PIPE)
    return process

def vlc_play(stdin):
    #print type(stdin)
    command = ["vlc", "-"]
    #print "vlc_play = %s" % " ".join(command)
    process = sp.Popen(command, stdin = tee(stdin, "GHOST.flv").stdout, stdout = open("/dev/null"), stderr = open("/dev/null"), shell = False)

def cat(stdin, path):
    #print type(stdin)
    command = ["cat"]
    #print "vlc_play = %s" % " ".join(command)
    process = sp.Popen(command, stdin = stdin, stdout = open(path), stderr = sp.PIPE, shell = False)


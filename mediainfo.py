#!/bin/env python2

import re
import subprocess

def parse_pixel(value):
    return int(re.search("^([0-9 ]+) pixels$", value).group(1).replace(" ", ""))

def parse_fps(value):
    return float(re.search("^([0-9 .]+) fps$", value).group(1))


MAPPINGS = {
    "Height": {
        "attribute": "height",
        "parser": parse_pixel
    },
    "Width": {
        "attribute": "width", 
        "parser": parse_pixel
    }, 
    "Frame rate": {
        "attribute": "frame_rate", 
        "parser": parse_fps
    }
}

class MediaInfo:

    def __init__(self, path):
        command = ["mediainfo", path]
        mediainfo = subprocess.Popen(command, stdout=subprocess.PIPE)
        output = mediainfo.stdout
        lines = map(lambda line: str(line), output.readlines())
        output.close()
        for key, value in self.parse_lines(lines):
            try:
                mapping = MAPPINGS[key]
                setattr(self, mapping["attribute"], mapping["parser"](value))
            except KeyError:
                continue
        

    def parse_lines(self, lines):
        return map(self.parse_line, lines)

    def parse_line(self, line):
        index = line.rfind(":")
        key = line[:index].strip()
        value = line[index + 1:].strip()
        return key, value
        
if __name__ == "__main__":
    mi = MediaInfo(b"TEST.flv")
    print(mi.height)
    print mi.frame_rate

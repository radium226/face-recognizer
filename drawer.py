#!/bin/env python2

import cv2
from scipy import interpolate

from commons import Frame, Size, Image, Coordinate, Rectangle
from detectors import FaceDetector

drawing = False
positions = []

STEP = 100

class PositionInterpolator(object):
    
    def __init__(self, f_x, f_y):
        self.f_x = f_x
        self.f_y = f_y

    def __call__(self, t):
        return Coordinate(int(self.f_x(t)), int(self.f_y(t)))

def position_interpolator(background):

    def callback(event, x, y, flags, parameters):
        global positions
        if event == 1: #cv2.EVENT_RBUTTONDOWN:
            positions.append(Coordinate(x, y))
    
    cv2.namedWindow("Interpolator")
    cv2.setMouseCallback("Interpolator", callback)

    while True: 
        cv2.imshow("Interpolator", background.array)
        if cv2.waitKey() & 0xFF == 27:
            break
    cv2.destroyWindow("Interpolator")

    t = map(lambda i: i * STEP, range(len(positions)))
    x = map(lambda p: p.x, positions)
    y = map(lambda p: p.y, positions)

    f_x = interpolate.interp1d(t, x, kind = "quadratic")
    f_y = interpolate.interp1d(t, y, kind = "quadratic")
    
    return PositionInterpolator(f_x, f_y)

def main():
    background = Image.blank(Size(500, 500))
    danielle = Image.open("../tests/Danielle.jpg")
    interpolator = position_interpolator(background)
    for t in range((len(positions) - 1) * STEP):
        if 100 < t < 150:
            image = background
        else:
            position = interpolator(t)
            danielle.position = position
            image = background.draw_image(danielle)
        image.show(10)
    #background = Image(Frame.blank(1, Size(500, 300)).array)
    #background.show()

    #danielle = Image.open("../tests/Danielle.jpg")
    #faces = FaceDetector.for_image(danielle).detect_faces()
    #face = faces[0]

    #danielle.crop(rectangle = face).show()    

    #dice.position = Coordinate(0, 20)
    #background.draw_image(dice).show()
    #dice.crop(position = Coordinate(10, 10)).show()


if __name__ == "__main__":
    main()

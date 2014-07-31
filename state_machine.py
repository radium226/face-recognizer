#!/bin/env python2

from collections import deque
import sys
from scipy import interpolate
from ring_buffer import RingBuffer, none_grouper

STATE = 0
CONDITION = 1
ACTION = 2
NEXT_STATE = 3

BUFFER_FILLED_SIZE = 2
SEQUENCE_BREAK_MAX_SIZE = 5
RANGE_SIZE = 50

SEQUENCE_BREAKS = [
    (5, 10), 
    (20, 35), 
    (37, 40)
]

def none_number_grouper(lp, rp):
    return none_grouper(lp[1], rp[1])

class AllConditions:

    def __init__(self, conditions):
        #print "conditions = %s" % conditions
        self.conditions = conditions

    @staticmethod
    def for_functions(functions):
        return AllConditions(functions)

    def __call__(self, index, number):
        result = True
        for condition in self.conditions:
            result = result and condition(index, number)
        return result
    
    @property
    def __name__(self):
        name = ""
        for condition in self.conditions:
            try:
                name += (condition.__name___ + " && ")
            except:
                name += (("%s" % condition) + " && ")

        return name

def all_conditions(*conditions):
    return AllConditions.for_functions(list(conditions))
        

class Transformer:

    FIRST_INDEX = "FIRST_INDEX"
    SEQUENCE = "SEQUENCE"
    NO_SEQUENCE = "NO_SEQUENCE"
    SEQUENCE_BREAK = "SEQUENCE_BREAK"
    BUFFERING_SEQUENCE_BREAK = "BUFFERING_SEQUENCE_BREAK"

    def __init__(self):
        self.state = Transformer.FIRST_INDEX
        self.transitions = [
            [Transformer.FIRST_INDEX, self.number_exists, self.return_number_as_is, Transformer.SEQUENCE], 
            [Transformer.FIRST_INDEX, self.number_not_exists, self.return_number_as_is, Transformer.NO_SEQUENCE], 
            [Transformer.SEQUENCE, self.number_exists, self.return_number_as_is, Transformer.SEQUENCE], 
            [Transformer.SEQUENCE, self.number_not_exists, self.return_nothing, Transformer.SEQUENCE_BREAK], 
            [Transformer.SEQUENCE_BREAK, self.number_exists, self.return_number_as_is, Transformer.BUFFERING_SEQUENCE_BREAK], #Brain Fuck
            [Transformer.SEQUENCE_BREAK, self.number_not_exists, self.return_nothing, Transformer.SEQUENCE_BREAK], 
            [Transformer.BUFFERING_SEQUENCE_BREAK, all_conditions(self.number_exists, self.is_buffer_not_filled), self.return_number_as_is, Transformer.BUFFERING_SEQUENCE_BREAK], 
            [Transformer.BUFFERING_SEQUENCE_BREAK, all_conditions(self.number_exists, self.is_buffer_filled), self.interpolate_numbers, Transformer.SEQUENCE], 
            [Transformer.BUFFERING_SEQUENCE_BREAK, self.number_not_exists, self.return_buffered_numbers_as_is, Transformer.NO_SEQUENCE],
            [Transformer.NO_SEQUENCE, self.number_exists, self.return_number_as_is, Transformer.SEQUENCE], 
            [Transformer.NO_SEQUENCE, self.number_not_exists, self.return_number_as_is, Transformer.NO_SEQUENCE]
        ]
        self.buffer = RingBuffer(length = BUFFER_FILLED_SIZE * 2 + SEQUENCE_BREAK_MAX_SIZE)

    def number_exists(self, index, number):
        return number is not None

    def number_not_exists(self, index, number):
        return not self.number_exists(index, number)

    def return_number_as_is(self, index, number):
        self.buffer.add_item((index, number))
        return [(index, number)]

    def return_nothing(self, index, number):
        self.buffer.add_item((index, number))
        return []

    def interpolate_numbers(self, index, number):
        groups = self.buffer.last_item_groups(3, grouper = none_number_grouper)
        extraction = sum(groups, [])
        e = filter(lambda p: p[1] is not None, extraction)
        t = map(lambda p: p[0], e)
        n = map(lambda p: p[1], e)
        f = interpolate.interp1d(t, n)

        d = []
        for j, _ in groups[1]:
            d.append((j, f(j)))
    
        self.buffer.add_item((index, number))
        return d + [(index, number)]

    def is_buffer_filled(self, index, number):
        group = self.buffer.last_item_group(grouper = none_number_grouper)
        print "group = %s" % group
        return len(group) >= BUFFER_FILLED_SIZE

    def is_buffer_not_filled(self, index, number):
        return not self.is_buffer_filled(index, number)

    def return_buffered_numbers_as_is(self, index, number):
        self.buffer.add_item((index, number))
        groups = self.buffer.last_item_groups(2, grouper = none_number_grouper)
        print " =========> group = %s" % groups
        return groups[1]

    def is_sequence_break_too_long(self, index, number):
        sequence_break = self.buffer.last_item_group(grouper = none_number_grouper)
        return len(sequence_break) > SEQUENCE_BREAK_MAX_SIZE    
    
    def is_sequence_break_short_enough(self, index, number):
        return not self.is_sequence_break_too_long(index, number)

    def transform(self, index, number):
        print "transform(%s, %s)" % (index, number)
        for transition in self.transitions:
            if transition[STATE] == self.state:
                print "self.state = %s" % self.state
                if transition[CONDITION](index, number):
                    print " ---> %s success" % transition[CONDITION].__name__
                    pairs = transition[ACTION](index, number)
                    next_state = transition[NEXT_STATE]
                    self.state = next_state
                    return pairs
                else:
                    print " ---> %s failed" % transition[CONDITION].__name__

        raise Exception("Unable to continue")

def numbers():
    sequence_break = False
    for n in range(RANGE_SIZE):
        if n in map(lambda b: b[0], SEQUENCE_BREAKS):
            sequence_break = True

        if n in map(lambda b: b[1], SEQUENCE_BREAKS):
            sequence_break = False

        yield n if not sequence_break else None

def transform_numbers():
    transformer = Transformer()
    for index, number in enumerate(numbers()):
        for index, number in transformer.transform(index, number):
            yield (index, number)

def main():
    for index, number in sorted(transform_numbers(), key = lambda p: p[0]):
        print "%s --> %s" % (index, number)
        
if __name__ == "__main__":
    main()

#!/bin/env python2

from collections import deque

def none_grouper(previous_item, item):
    return (previous_item is None and item is None) or (previous_item is not None and item is not None)

class RingBuffer:
    
    def __init__(self, length):
        self.deque = deque(maxlen = length)
        for i in range(length):
            self.deque.append(None)

    def add_item(self, item):
        self.deque.append(item)

    def last_items(self, length):
        return [self.deque[i] for i in map(lambda index: index + len(self.deque) - length, range(length))]

    def __str__(self):
        return self.deque.__str__()

    def items(self, first_index, length):
        return [self.deque[i] for i in map(lambda index: index + first_index, range(length))]

    def item_groups(self, grouper = none_grouper):
        groups = []
        group = []
        
        previous_item = None
        for index, item in enumerate(self.deque):
            if index == 0: 
                group.append(item)
            else:
                if grouper(previous_item, item):
                    group.append(item)
                else:
                    groups.append(group)
                    group = [item]
            previous_item = item
        groups.append(group)
        return groups

    def last_item_groups(self, count, grouper = none_grouper):
        item_groups = self.item_groups(grouper)
        return item_groups[len(item_groups) - count:len(item_groups)]
    
    def last_item_group(self, grouper = none_grouper):
        return self.last_item_groups(1, grouper)[0]

if __name__ == "__main__":
    rb = RingBuffer(10)
    rb.add_item(0)
    rb.add_item(10)
    rb.add_item(100)
    rb.add_item(1000)
    rb.add_item(10000)
    rb.add_item(None)
    rb.add_item("a")
    print rb.item_groups()
    

    print rb.last_item_groups(2)

    


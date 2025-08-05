class Box:
    def __init__(self):
        self.item_instance = self.Gadget()
    class Gadget:
        def __init__(self):
            print("Gadget has been created!")
        def operate(self):
            print("Gadget is now operating!")
        def stop(self):
            print("Gadget has stopped!")
box_instance = Box()
gadget_instance = box_instance.item_instance
gadget_instance.operate()
gadget_instance.stop()

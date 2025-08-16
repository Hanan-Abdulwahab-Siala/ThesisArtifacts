public class Container {
    private int containerData;
    public Container(int containerData) {
        this.containerData = containerData;
    }
    public class Item {
        private int itemData;
        public Item(int itemData) {
            this.itemData = itemData;
        }
        public void display() {
            System.out.println("Container Data: " + containerData);
            System.out.println("Item Data: " + itemData);
        }
    }
}

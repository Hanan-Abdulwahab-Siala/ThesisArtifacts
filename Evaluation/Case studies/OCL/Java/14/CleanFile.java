interface ActionListener {
    int ACTION_ADD = 0;
    int ACTION_REMOVE = 1;
    void actionSelected(int action);
}
interface RequestListener {
    int requestReceived();
}
class ActionHandler implements ActionListener, RequestListener {
    public void actionSelected(int action) {
    }
    public int requestReceived() {
    }
}
class Dummy {
    public void dummy() {
        RequestListener listener = new ActionHandler(); 
        listener.requestReceived(); 
    }
}
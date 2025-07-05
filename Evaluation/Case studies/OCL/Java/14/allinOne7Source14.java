
// Interfaces -------------------
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
        //Calling method defined by interface
        RequestListener listener = new ActionHandler(); /*ActionHandler can be
                                           represented as RequestListener...*/
        listener.requestReceived(); /*...and thus is known to implement
                                    requestReceived() method*/
    }
}


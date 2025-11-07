import java.util.*;
abstract class AbstractContract {
   public boolean validateTransaction(int senderID, int receiverID) {
      return senderID != receiverID;
   }
}
class Payment extends AbstractContract {
   private int senderID;
   private int receiverID;
   public int getSenderID() {
      return senderID;
   }
   public int getReceiverID() {
      return receiverID;
   }
}

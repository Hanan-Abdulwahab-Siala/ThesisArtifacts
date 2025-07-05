class BankAccount:
    account_number_counter = 1000
    def __init__(self, balance=0):
        self.account_number = BankAccount.account_number_counter
        BankAccount.account_number_counter += 1
        self.balance = balance
    def deposit(self, amount):
        if amount > 0:
            self.balance += amount
            print(f"Deposited {amount}. New balance: {self.balance}")
        else:
            print("Deposit amount must be positive")
    def withdraw(self, amount):
        if 0 < amount <= self.balance:
            self.balance -= amount
            print(f"Withdrew {amount}. New balance: {self.balance}")
        else:
            print("Insufficient funds or invalid amount")
    def check_balance(self):
        return self.balance
class SavingsAccount(BankAccount):
    def __init__(self, balance=0, interest_rate=0.01):
        super().__init__(balance)
        self.interest_rate = interest_rate
    def apply_interest(self):
        interest = self.balance * self.interest_rate
        self.balance += interest
        print(f"Applied interest {interest}. New balance: {self.balance}")
class User:
    def __init__(self, user_id, name):
        self.user_id = user_id
        self.name = name
        self.accounts = []
    def add_account(self, account):
        self.accounts.append(account)
        print(f"Account {account.account_number} added to user {self.name}")
    def remove_account(self, account_number):
        self.accounts = [acc for acc in self.accounts if acc.account_number != account_number]
        print(f"Account {account_number} removed from user {self.name}")
    def get_accounts(self):
        return self.accounts
class Bank:
    def __init__(self):
        self.users = []
    def add_user(self, user):
        self.users.append(user)
        print(f"User {user.name} added")
    def find_user(self, user_id):
        for user in self.users:
            if user.user_id == user_id:
                return user
        return None
    def transfer(self, from_account, to_account, amount):
        if from_account.balance >= amount:
            from_account.withdraw(amount)
            to_account.deposit(amount)
            print(f"Transferred {amount} from account {from_account.account_number} to account {to_account.account_number}")
        else:
            print("Insufficient funds for transfer")
bank = Bank()
user1 = User(1, "Alice")
user2 = User(2, "Bob")
bank.add_user(user1)
bank.add_user(user2)
account1 = BankAccount()
account2 = SavingsAccount(interest_rate=0.05)
user1.add_account(account1)
user2.add_account(account2)
account1.deposit(1000)
account1.withdraw(500)
account2.deposit(2000)
account2.apply_interest()
bank.transfer(account1, account2, 200)
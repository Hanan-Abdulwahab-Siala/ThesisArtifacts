from abc import ABC, abstractmethod
class Person(ABC):
    def __init__(self, name, email):
        self.name = name
        self.email = email
    @abstractmethod
    def get_details(self):
        pass
class Member(Person):
    def __init__(self, name, email, member_id):
        super().__init__(name, email)
        self.member_id = member_id
        self.__borrowed_books = []
    def borrow_book(self, book):
        if book.is_available():
            self.__borrowed_books.append(book)
            book.borrow(self)
            print(f"{self.name} borrowed '{book.title}'.")
        else:
            print(f"'{book.title}' is not available.")
    def return_book(self, book):
        if book in self.__borrowed_books:
            self.__borrowed_books.remove(book)
            book.return_book()
            print(f"{self.name} returned '{book.title}'.")
        else:
            print(f"{self.name} did not borrow '{book.title}'.")
    def get_details(self):
        return f"Member ID: {self.member_id}, Name: {self.name}, Email: {self.email}"
    def get_borrowed_books(self):
        return [book.title for book in self.__borrowed_books]
class Book:
    def __init__(self, title, author, isbn):
        self.title = title
        self.author = author
        self.isbn = isbn
        self.__borrower = None
    def is_available(self):
        return self.__borrower is None
    def borrow(self, member):
        self.__borrower = member
    def return_book(self):
        self.__borrower = None
    def get_borrower(self):
        return self.__borrower
    def __str__(self):
        return f"Title: {self.title}, Author: {self.author}, ISBN: {self.isbn}"
class Library:
    def __init__(self):
        self.__books = []
        self.__members = []
    def add_book(self, book):
        self.__books.append(book)
        print(f"Added book: {book.title}.")
    def add_member(self, member):
        self.__members.append(member)
        print(f"Added member: {member.name}.")
    def list_books(self):
        print("\n=== List of Books ===")
        for book in self.__books:
            print(book)
    def list_available_books(self):
        print("\n=== List of Available Books ===")
        for book in self.__books:
            if book.is_available():
                print(book)
    def list_members(self):
        print("\n=== List of Members ===")
        for member in self.__members:
            print(member.get_details())
def main():
    library = Library()
    book1 = Book(title="The Great Gatsby", author="F. Scott Fitzgerald", isbn="9780743273565")
    book2 = Book(title="To Kill a Mockingbird", author="Harper Lee", isbn="9780061120084")
    book3 = Book(title="1984", author="George Orwell", isbn="9780451524935")
    library.add_book(book1)
    library.add_book(book2)
    library.add_book(book3)
    member1 = Member(name="Alice Johnson", email="alice.j@example.com", member_id="M001")
    member2 = Member(name="Bob Smith", email="bob.s@example.com", member_id="M002")
    library.add_member(member1)
    library.add_member(member2)
    library.list_books()
    library.list_members()
    member1.borrow_book(book1)
    member2.borrow_book(book2)
    library.list_available_books()
    member1.return_book(book1)
    library.list_available_books()
if __name__ == "__main__":
    main()
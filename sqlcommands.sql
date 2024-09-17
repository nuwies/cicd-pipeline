-- Question table
CREATE TABLE questions(
    Id INT NOT NULL AUTO_INCREMENT,
    Category VARCHAR(255) NOT NULL,
    Question VARCHAR(255) NOT NULL,
    CorrectAnswer VARCHAR(255) NOT NULL,
    WrongAnswer1 VARCHAR(255) NOT NULL,
    WrongAnswer2 VARCHAR(255),
    WrongAnswer3 VARCHAR(255),
    ContentName VARCHAR(255),
    DateCreated TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY(Id),
    FOREIGN KEY(Category) REFERENCES categories(name)
);

-- Categories table
CREATE TABLE categories (
    name VARCHAR(255) PRIMARY KEY,         
    contentpath VARCHAR(255) NOT NULL,     
    datecreated TIMESTAMP DEFAULT CURRENT_TIMESTAMP 
);

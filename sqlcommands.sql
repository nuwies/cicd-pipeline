-- Assumes that the root user has no password

-- Database we are using
CREATE DATABASE assignment1;

CONNECT assignment1;

-- Question table
CREATE TABLE questions(
    Question VARCHAR(255),
    CorrectAnswer VARCHAR(255),
    WrongAnswer1 VARCHAR(255),
    WrongAnswer2 VARCHAR(255),
    WrongAnswer3 VARCHAR(255),
    ContentName VARCHAR(255),
    DateCreated TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY(question)
);

-- Categories table
CREATE TABLE categories (
    name VARCHAR(255) PRIMARY KEY,         
    contentpath VARCHAR(255) NOT NULL,     
    datecreated TIMESTAMP DEFAULT CURRENT_TIMESTAMP 
);

INSERT INTO categories (name, contentpath) VALUES
('40\'s Movies', '/path/to/40s_movies_quiz'),
('50\'s Politics', '/path/to/50s_politics_quiz'),
('60\'s Products', '/path/to/60s_products_quiz');

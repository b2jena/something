-- Database initialization script
-- Creates indexes and sample data for the Book API

-- Create additional indexes for better query performance
CREATE INDEX IF NOT EXISTS idx_books_title_author ON books(title, author);
CREATE INDEX IF NOT EXISTS idx_books_price_range ON books(price) WHERE price > 0;
CREATE INDEX IF NOT EXISTS idx_books_created_at ON books(created_at);

-- Insert sample data for testing
INSERT INTO books (title, author, isbn, price, category, description, stock_quantity, version, created_at, updated_at)
VALUES ('Spring Boot in Action', 'Craig Walls', '9781617292545', 39.99, 'Technology',
        'Comprehensive guide to Spring Boot development', 25, 0, NOW(), NOW()),
       ('Clean Code', 'Robert C. Martin', '9780132350884', 42.99, 'Technology',
        'A handbook of agile software craftsmanship', 15, 0, NOW(), NOW()),
       ('Effective Java', 'Joshua Bloch', '9780134685991', 54.99, 'Technology', 'Best practices for the Java platform',
        30, 0, NOW(), NOW()),
       ('Design Patterns', 'Gang of Four', '9780201633612', 59.99, 'Technology',
        'Elements of reusable object-oriented software', 20, 0, NOW(), NOW()),
       ('The Pragmatic Programmer', 'David Thomas', '9780135957059', 49.99, 'Technology', 'Your journey to mastery', 18,
        0, NOW(), NOW()),
       ('Microservices Patterns', 'Chris Richardson', '9781617294549', 44.99, 'Technology', 'With examples in Java', 22,
        0, NOW(), NOW()),
       ('Java Concurrency in Practice', 'Brian Goetz', '9780321349606', 52.99, 'Technology',
        'Essential guide to concurrent programming', 12, 0, NOW(), NOW()),
       ('Spring Security in Action', 'Laurentiu Spilca', '9781617297731', 47.99, 'Technology',
        'Secure your applications', 28, 0, NOW(), NOW()),
       ('Building Microservices', 'Sam Newman', '9781491950357', 41.99, 'Technology', 'Designing fine-grained systems',
        16, 0, NOW(), NOW()),
       ('Docker Deep Dive', 'Nigel Poulton', '9781521822807', 35.99, 'Technology', 'Zero to Docker in a single book',
        24, 0, NOW(), NOW()) ON CONFLICT (isbn) DO NOTHING;
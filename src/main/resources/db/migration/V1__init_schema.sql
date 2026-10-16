create table users(
    id UUID PRIMARY KEY ,
    username VARCHAR(100) NOT NULL UNIQUE ,
    email  VARCHAR(255) NOT NULL UNIQUE ,
    password VARCHAR(255) NOT NULL,
    first_name VARCHAR(255) NOT NULL ,
    last_name VARCHAR(255) NOT NULL ,
    role VARCHAR(50) NOT NULL ,
    status VARCHAR(50) NOT NULL ,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL
);

create table events(
    id UUID PRIMARY KEY ,
    title VARCHAR(255) NOT NULL ,
    description VARCHAR(2000)  ,
    status VARCHAR(50) NOT NULL ,
    start_time TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    end_time TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    total_capacity INT NOT NULL
);





CREATE TYPE task_status AS ENUM (
    'CREATED',
    'WAITING_FOR_APPROVE',
    'APPROVED',
    'TO_DO',
    'IN_PROGRESS',
    'RESOLVED',
    'COMPLETED',
    'TRASHED'
    );

CREATE TYPE task_type AS ENUM (
    'ANIMAL',
    'PEOPLE',
    'ENVIRONMENT',
    'PLANT',
    'URBAN',
    'OTHER'
    );

CREATE TYPE vote_type AS ENUM (
    'APPROVE',
    'REJECT'
    );

CREATE TYPE item_type AS ENUM (
    'TECH',
    'SALE',
    'COUPON',
    'OTHER'
    );

CREATE TYPE account_status AS ENUM (
    'FRIEZED',
    'ACTIVE',
    'BLOCKED'
    );

CREATE TABLE public.task
(
    id          BIGSERIAL PRIMARY KEY,
    title       VARCHAR,
    description VARCHAR,
    status      public.task_status NOT NULL,
    coordinate  point              NOT NULL,
    reward      BIGINT             NOT NULL,
    assignee    VARCHAR,
    type        public.task_type   NOT NULL,
    due_date    timestamp,
    updated     timestamp,
    created_by  VARCHAR            NOT NULL,
    created     timestamp          NOT NULL DEFAULT now()
);

CREATE INDEX ON public.task (status, assignee, created_by);
CREATE INDEX ON public.task (assignee, created_by);
CREATE INDEX ON public.task (created_by);

CREATE TABLE public.attachment
(
    id      BIGSERIAL PRIMARY KEY,
    task_id BIGINT REFERENCES public.task (id) ON DELETE CASCADE,
    content BYTEA,
    type    VARCHAR,
    length  BIGINT
);

CREATE TABLE public.item
(
    id          BIGSERIAL PRIMARY KEY,
    title       VARCHAR,
    description VARCHAR,
    status      public.task_status NOT NULL,
    price       BIGINT             NOT NULL,
    type        public.item_type   NOT NULL,
    amount      BIGINT             NOT NULL,
    created_by  VARCHAR            NOT NULL
);

CREATE TABLE public.item_attachment
(
    id      BIGSERIAL PRIMARY KEY,
    item_id BIGINT REFERENCES public.task (id) ON DELETE CASCADE,
    content BYTEA,
    type    VARCHAR,
    length  BIGINT
);

CREATE TABLE public.order
(
    customer VARCHAR   NOT NULL,
    item_id  BIGINT    NOT NULL REFERENCES public.item (id),
    created  TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE public.approval
(
    task_id BIGINT PRIMARY KEY,
    status  public.task_status    NOT NULL,
    counter BIGINT    DEFAULT (0) NOT NULL,
    created timestamp DEFAULT now(),
    CONSTRAINT unique_task_id_constr UNIQUE (task_id)
);

CREATE TABLE public.vote
(
    client_id VARCHAR,
    task_id   BIGINT           NOT NULL,
    type      public.vote_type NOT NULL,
    PRIMARY KEY (task_id, client_id)
);

CREATE TABLE public.account
(
    user_id VARCHAR PRIMARY KEY   NOT NULL,
    amount  BIGINT                NOT NULL DEFAULT (0),
    status  public.account_status NOT NULL DEFAULT 'ACTIVE'::account_status,
    updated timestamp,
    created timestamp             NOT NULL DEFAULT now()
);

CREATE TABLE public.event
(
    user_id   VARCHAR NOT NULL REFERENCES public.account (user_id),
    value     BIGINT  NOT NULL,
    name      VARCHAR,
    initiator VARCHAR,
    timestamp timestamp
);

/* For test population */
CREATE OR REPLACE FUNCTION populate_tasks(n INTEGER) RETURNS VOID
    LANGUAGE plpgsql
AS
$$
DECLARE
    counter INTEGER := 0;
BEGIN
    LOOP
        EXIT WHEN counter = n;
        counter := counter + 1;
        INSERT INTO public.task (title, description, status, coordinate, reward, assignee, type, due_date, created_by)
        VALUES (CONCAT('Task ', counter),
                CONCAT('Some description of Task ', counter),
                'WAITING_FOR_APPROVE',
                point(random() * 261 - 180, random() * 181 - 90),
                counter * 100,
                floor(random() * 1001),
                (SELECT t FROM unnest(enum_range(NULL::task_type)) t ORDER BY random() LIMIT 1),
                (SELECT * FROM generate_series('2020-08-01'::timestamp, '2021-10-01'::timestamp, '1 day'::interval) LIMIT 1),
                floor(random() * 1001));
    END LOOP;
END;
$$;

select public.populate_tasks(1000);

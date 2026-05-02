-- WARNING: This schema is for context only and is not meant to be run.
-- Table order and constraints may not be valid for execution.

CREATE TABLE public.budgets (
id uuid NOT NULL DEFAULT gen_random_uuid(),
user_id uuid NOT NULL,
category_id uuid NOT NULL,
amount_limit double precision NOT NULL,
month integer NOT NULL CHECK (month >= 1 AND month <= 12),
year integer NOT NULL,
created_at timestamp with time zone NOT NULL DEFAULT now(),
updated_at timestamp with time zone NOT NULL DEFAULT now(),
is_deleted boolean NOT NULL DEFAULT false,
CONSTRAINT budgets_pkey PRIMARY KEY (id),
CONSTRAINT budgets_user_id_fkey FOREIGN KEY (user_id) REFERENCES auth.users(id),
CONSTRAINT budgets_category_id_fkey FOREIGN KEY (category_id) REFERENCES public.categories(id)
);
CREATE TABLE public.categories (
id uuid NOT NULL DEFAULT gen_random_uuid(),
user_id uuid NOT NULL,
name text NOT NULL,
type text NOT NULL CHECK (type = ANY (ARRAY['expense'::text, 'income'::text])),
icon text NOT NULL,
color text NOT NULL,
is_system boolean NOT NULL DEFAULT false,
created_at timestamp with time zone NOT NULL DEFAULT now(),
updated_at timestamp with time zone NOT NULL DEFAULT now(),
is_deleted boolean NOT NULL DEFAULT false,
CONSTRAINT categories_pkey PRIMARY KEY (id),
CONSTRAINT categories_user_id_fkey FOREIGN KEY (user_id) REFERENCES auth.users(id)
);
CREATE TABLE public.profiles (
id uuid NOT NULL,
username text UNIQUE,
created_at timestamp with time zone NOT NULL DEFAULT timezone('utc'::text, now()),
CONSTRAINT profiles_pkey PRIMARY KEY (id),
CONSTRAINT profiles_id_fkey FOREIGN KEY (id) REFERENCES auth.users(id)
);
CREATE TABLE public.transactions (
id uuid NOT NULL DEFAULT gen_random_uuid(),
user_id uuid NOT NULL,
wallet_id uuid NOT NULL,
category_id uuid NOT NULL,
amount double precision NOT NULL,
type text NOT NULL CHECK (type = ANY (ARRAY['expense'::text, 'income'::text])),
note text DEFAULT ''::text,
transaction_date timestamp with time zone NOT NULL,
ai_generated boolean NOT NULL DEFAULT false,
created_at timestamp with time zone NOT NULL DEFAULT now(),
updated_at timestamp with time zone NOT NULL DEFAULT now(),
is_deleted boolean NOT NULL DEFAULT false,
CONSTRAINT transactions_pkey PRIMARY KEY (id),
CONSTRAINT transactions_user_id_fkey FOREIGN KEY (user_id) REFERENCES auth.users(id),
CONSTRAINT transactions_wallet_id_fkey FOREIGN KEY (wallet_id) REFERENCES public.wallets(id),
CONSTRAINT transactions_category_id_fkey FOREIGN KEY (category_id) REFERENCES public.categories(id)
);
CREATE TABLE public.wallets (
id uuid NOT NULL DEFAULT gen_random_uuid(),
user_id uuid NOT NULL,
name text NOT NULL,
balance double precision NOT NULL DEFAULT 0.0,
icon text NOT NULL,
color text NOT NULL,
is_default boolean NOT NULL DEFAULT false,
created_at timestamp with time zone NOT NULL DEFAULT now(),
updated_at timestamp with time zone NOT NULL DEFAULT now(),
is_deleted boolean NOT NULL DEFAULT false,
CONSTRAINT wallets_pkey PRIMARY KEY (id),
CONSTRAINT wallets_user_id_fkey FOREIGN KEY (user_id) REFERENCES auth.users(id)
);
-- Supabase SQL Schema for `profiles` Table and Auth Hooks
-- Run this script in your Supabase SQL Editor (https://supabase.com/dashboard/project/_/sql)

-- 1. Create profiles table referencing auth.users
CREATE TABLE IF NOT EXISTS public.profiles (
    id UUID PRIMARY KEY REFERENCES auth.users(id) ON DELETE CASCADE,
    email TEXT,
    name TEXT NOT NULL DEFAULT '',
    handle TEXT UNIQUE,
    bio TEXT DEFAULT '',
    location TEXT DEFAULT '',
    avatar_url TEXT,
    banner_url TEXT,
    is_verified BOOLEAN NOT NULL DEFAULT FALSE,
    following_count INTEGER NOT NULL DEFAULT 0,
    followers_count INTEGER NOT NULL DEFAULT 0,
    joined_date TEXT DEFAULT 'Joined recently',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT timezone('utc'::text, now())
);

-- 2. Indexes for fast user queries
CREATE INDEX IF NOT EXISTS idx_profiles_handle ON public.profiles(handle);
CREATE INDEX IF NOT EXISTS idx_profiles_email ON public.profiles(email);
CREATE INDEX IF NOT EXISTS idx_profiles_created_at ON public.profiles(created_at DESC);

-- 3. Enable Row Level Security (RLS)
ALTER TABLE public.profiles ENABLE ROW LEVEL SECURITY;

-- 4. Access Policies
DROP POLICY IF EXISTS "Public Profiles Read Access" ON public.profiles;
CREATE POLICY "Public Profiles Read Access"
    ON public.profiles FOR SELECT
    USING (true);

DROP POLICY IF EXISTS "Users Can Insert Own Profile" ON public.profiles;
CREATE POLICY "Users Can Insert Own Profile"
    ON public.profiles FOR INSERT
    WITH CHECK (auth.uid() = id);

DROP POLICY IF EXISTS "Users Can Update Own Profile" ON public.profiles;
CREATE POLICY "Users Can Update Own Profile"
    ON public.profiles FOR UPDATE
    USING (auth.uid() = id);

-- 5. Trigger: Automatically insert profile when a new user signs up via Supabase Auth
CREATE OR REPLACE FUNCTION public.handle_new_user()
RETURNS trigger AS $$
BEGIN
    INSERT INTO public.profiles (
        id,
        email,
        name,
        handle,
        joined_date
    )
    VALUES (
        new.id,
        new.email,
        COALESCE(new.raw_user_meta_data->>'name', split_part(new.email, '@', 1)),
        COALESCE(new.raw_user_meta_data->>'handle', split_part(new.email, '@', 1)),
        to_char(now(), 'FMMonth YYYY')
    )
    ON CONFLICT (id) DO UPDATE
    SET email = EXCLUDED.email;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

DROP TRIGGER IF EXISTS on_auth_user_created ON auth.users;
CREATE TRIGGER on_auth_user_created
    AFTER INSERT ON auth.users
    FOR EACH ROW EXECUTE PROCEDURE public.handle_new_user();

-- 6. Enable Realtime updates for profiles table
ALTER PUBLICATION supabase_realtime ADD TABLE public.profiles;

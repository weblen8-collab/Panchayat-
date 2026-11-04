-- Supabase SQL Schema for `posts` Table
-- Run this in your Supabase SQL Editor (https://supabase.com/dashboard/project/_/sql)

-- 1. Create table
CREATE TABLE IF NOT EXISTS public.posts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    author_id TEXT NOT NULL,
    content TEXT NOT NULL,
    timestamp BIGINT DEFAULT (EXTRACT(EPOCH FROM now()) * 1000)::BIGINT,
    likes_count INTEGER NOT NULL DEFAULT 0,
    comments_count INTEGER NOT NULL DEFAULT 0,
    reposts_count INTEGER NOT NULL DEFAULT 0,
    views_count INTEGER NOT NULL DEFAULT 0,
    author_name TEXT NOT NULL DEFAULT '',
    author_handle TEXT NOT NULL DEFAULT '',
    author_avatar_url TEXT,
    is_verified BOOLEAN NOT NULL DEFAULT FALSE,
    category TEXT NOT NULL DEFAULT 'General',
    media_url TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT timezone('utc'::text, now())
);

-- 2. Indexes for queries and sorting
CREATE INDEX IF NOT EXISTS idx_posts_author_id ON public.posts(author_id);
CREATE INDEX IF NOT EXISTS idx_posts_timestamp ON public.posts(timestamp DESC);
CREATE INDEX IF NOT EXISTS idx_posts_created_at ON public.posts(created_at DESC);
CREATE INDEX IF NOT EXISTS idx_posts_category ON public.posts(category);

-- 3. Row Level Security (RLS)
ALTER TABLE public.posts ENABLE ROW LEVEL SECURITY;

-- 4. Access Policies (Open read/write for client anon key communication)
DROP POLICY IF EXISTS "Public Read Access" ON public.posts;
CREATE POLICY "Public Read Access"
    ON public.posts FOR SELECT
    USING (true);

DROP POLICY IF EXISTS "Public Insert Access" ON public.posts;
CREATE POLICY "Public Insert Access"
    ON public.posts FOR INSERT
    WITH CHECK (true);

DROP POLICY IF EXISTS "Public Update Access" ON public.posts;
CREATE POLICY "Public Update Access"
    ON public.posts FOR UPDATE
    USING (true);

DROP POLICY IF EXISTS "Public Delete Access" ON public.posts;
CREATE POLICY "Public Delete Access"
    ON public.posts FOR DELETE
    USING (true);

-- 5. Realtime support (Optional, for real-time post subscriptions)
ALTER PUBLICATION supabase_realtime ADD TABLE public.posts;

package com.example.panchayat.data.model

import com.example.panchayat.data.local.entity.UserProfileEntity
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.util.UUID

/**
 * UserProfile data class representing a registered user in Supabase and the application.
 *
 * @param id Unique user identifier matching Supabase auth.users.id
 * @param email The user's registered email address
 * @param name Display name
 * @param handle Unique username / handle (without @)
 * @param bio Short biographical description
 * @param location Geographic location / village / city
 * @param avatarUrl Profile avatar image URL
 * @param bannerUrl Header banner image URL
 * @param isVerified Whether user has verified status badge
 * @param followingCount Count of accounts this user is following
 * @param followersCount Count of accounts following this user
 * @param joinedDate User-friendly joined date string (e.g. "Joined September 2024")
 * @param createdAt Supabase ISO timestamp of record creation
 */
@JsonClass(generateAdapter = true)
data class UserProfile(
    @Json(name = "id")
    val id: String = UUID.randomUUID().toString(),

    @Json(name = "email")
    val email: String? = null,

    @Json(name = "name")
    val name: String = "",

    @Json(name = "handle")
    val handle: String = "",

    @Json(name = "bio")
    val bio: String = "",

    @Json(name = "location")
    val location: String = "",

    @Json(name = "avatar_url")
    val avatarUrl: String? = null,

    @Json(name = "banner_url")
    val bannerUrl: String? = null,

    @Json(name = "is_verified")
    val isVerified: Boolean = false,

    @Json(name = "following_count")
    val followingCount: Int = 0,

    @Json(name = "followers_count")
    val followersCount: Int = 0,

    @Json(name = "joined_date")
    val joinedDate: String = "Joined recently",

    @Json(name = "created_at")
    val createdAt: String? = null
) {
    /**
     * Converts domain UserProfile into Room database entity.
     */
    fun toEntity(isCurrentUser: Boolean = false, isFollowing: Boolean = false): UserProfileEntity {
        val userHandle = handle.ifBlank { "user_${id.take(6)}" }
        return UserProfileEntity(
            id = id,
            handle = userHandle,
            name = name.ifBlank { "Panchayat Member" },
            username = userHandle,
            bio = bio,
            location = location,
            joinedDate = joinedDate,
            followingCount = followingCount,
            followersCount = followersCount,
            isVerified = isVerified,
            avatarUrl = avatarUrl,
            profileImageUrl = avatarUrl,
            bannerUrl = bannerUrl,
            isCurrentUser = isCurrentUser,
            isFollowing = isFollowing
        )
    }

    companion object {
        /**
         * Creates a UserProfile from a Room entity.
         */
        fun fromEntity(entity: UserProfileEntity, email: String? = null): UserProfile {
            val userHandle = entity.username.ifBlank { entity.handle }
            val userAvatar = entity.profileImageUrl ?: entity.avatarUrl
            return UserProfile(
                id = entity.id,
                email = email,
                name = entity.name.ifBlank { userHandle },
                handle = userHandle,
                bio = entity.bio,
                location = entity.location,
                avatarUrl = userAvatar,
                bannerUrl = entity.bannerUrl,
                isVerified = entity.isVerified,
                followingCount = entity.followingCount,
                followersCount = entity.followersCount,
                joinedDate = entity.joinedDate
            )
        }

        /**
         * Supabase PostgreSQL schema for the `profiles` table matching Supabase Auth.
         */
        const val SUPABASE_PROFILES_TABLE_SCHEMA: String = """
-- Supabase Profiles Table Schema (Linked to auth.users)
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

-- Enable RLS
ALTER TABLE public.profiles ENABLE ROW LEVEL SECURITY;

-- Policies
CREATE POLICY "Public Profiles Read Access"
    ON public.profiles FOR SELECT
    USING (true);

CREATE POLICY "Users Can Insert Own Profile"
    ON public.profiles FOR INSERT
    WITH CHECK (auth.uid() = id);

CREATE POLICY "Users Can Update Own Profile"
    ON public.profiles FOR UPDATE
    USING (auth.uid() = id);

-- Automatic Profile Creation Trigger on Supabase Auth Signup
CREATE OR REPLACE FUNCTION public.handle_new_user()
RETURNS trigger AS $$
BEGIN
    INSERT INTO public.profiles (id, email, name, handle, joined_date)
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
"""
    }
}

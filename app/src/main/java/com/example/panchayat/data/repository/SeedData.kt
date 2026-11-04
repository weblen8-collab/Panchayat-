package com.example.panchayat.data.repository

import com.example.R
import com.example.panchayat.data.local.entity.NotificationEntity
import com.example.panchayat.data.local.entity.PostEntity
import com.example.panchayat.data.local.entity.ReplyEntity
import com.example.panchayat.data.local.entity.UserProfileEntity

data class TrendingTag(
    val category: String,
    val tag: String,
    val postCountText: String
)

object SeedData {

    val currentUser = UserProfileEntity(
        id = "user_me",
        username = "aman_verma",
        name = "Aman Verma",
        handle = "aman_verma",
        bio = "Rural tech enthusiast & community volunteer. Exploring grassroots democracy & digital village initiatives in Panchayat 🇮🇳",
        location = "Phulera Gram Panchayat, Ballia",
        joinedDate = "Joined September 2024",
        followingCount = 142,
        followersCount = 389,
        isVerified = true,
        avatarUrl = null,
        profileImageUrl = null,
        bannerUrl = null,
        isCurrentUser = true,
        isFollowing = false
    )

    val suggestedUsers = listOf(
        UserProfileEntity(
            id = "user_sarpanch",
            username = "sarpanch_phulera",
            name = "Manju Devi",
            handle = "sarpanch_phulera",
            bio = "Elected Sarpanch of Phulera Gram Panchayat. Focused on clean drinking water, women self-help groups & primary education.",
            location = "Gram Panchayat Office",
            joinedDate = "Joined January 2024",
            followingCount = 45,
            followersCount = 2450,
            isVerified = true,
            isCurrentUser = false,
            isFollowing = true
        ),
        UserProfileEntity(
            id = "user_secretary",
            username = "sachiv_phulera",
            name = "Sachiv Ji (Abhishek)",
            handle = "sachiv_phulera",
            bio = "Panchayat Secretary (Sachiv). Handling government schemes, PM-Kisan verification & rural infrastructure audits.",
            location = "Panchayat Bhawan",
            joinedDate = "Joined March 2024",
            followingCount = 89,
            followersCount = 1890,
            isVerified = true,
            isCurrentUser = false,
            isFollowing = true
        ),
        UserProfileEntity(
            id = "user_pradhan",
            username = "pradhan_ji",
            name = "Brij Bhushan Dubey",
            handle = "pradhan_ji",
            bio = "Community elder & former Pradhan. Preserving village heritage, pond rejuvenation & dispute mediation.",
            location = "North Tola, Phulera",
            joinedDate = "Joined February 2024",
            followingCount = 210,
            followersCount = 3120,
            isVerified = true,
            isCurrentUser = false,
            isFollowing = false
        ),
        UserProfileEntity(
            id = "user_krishi",
            username = "krishi_vikas",
            name = "Gram Krishi Kendra",
            handle = "krishi_vikas",
            bio = "Daily mandi rates, organic fertilizers guide, drone spraying updates & monsoon forecasts for local farmers.",
            location = "Agricultural Extension Center",
            joinedDate = "Joined April 2024",
            followingCount = 12,
            followersCount = 4210,
            isVerified = true,
            isCurrentUser = false,
            isFollowing = false
        )
    )

    fun getInitialPosts(): List<PostEntity> {
        val now = System.currentTimeMillis()
        return listOf(
            PostEntity(
                id = "post_1",
                authorId = "user_sarpanch",
                authorName = "Manju Devi",
                authorHandle = "sarpanch_phulera",
                isVerified = true,
                content = "📢 Urgent Notice: The upcoming #GramSabha meeting is scheduled for Saturday at 10:00 AM at Panchayat Bhawan.\n\nAgenda items:\n1. Approval of 45 new solar street lights\n2. Renovation of primary school playground\n3. Finalizing water pipeline extensions\n\nEvery resident's participation matters! #PanchayatVikas",
                timestamp = now - (15 * 60 * 1000), // 15 mins ago
                category = "Gram Sabha",
                mediaDrawableRes = R.drawable.gram_sabha_meeting,
                likesCount = 142,
                repostsCount = 38,
                repliesCount = 19,
                viewsCount = 1840,
                isLiked = true,
                isReposted = false
            ),
            PostEntity(
                id = "post_2",
                authorId = "user_secretary",
                authorName = "Sachiv Ji (Abhishek)",
                authorHandle = "sachiv_phulera",
                isVerified = true,
                content = "Great news for our village! 🌟 The high-speed fiber internet connection at Panchayat Digital Center is officially live.\n\nStudents can now access digital textbooks and farmers can check e-NAM mandi prices in real-time. Transparent governance in action! #DigitalPanchayat #RuralTech",
                timestamp = now - (45 * 60 * 1000),
                category = "Development",
                mediaDrawableRes = R.drawable.panchayat_banner,
                likesCount = 289,
                repostsCount = 76,
                repliesCount = 34,
                viewsCount = 3420,
                isLiked = false,
                isReposted = true
            ),
            PostEntity(
                id = "post_3",
                authorId = "user_krishi",
                authorName = "Gram Krishi Kendra",
                authorHandle = "krishi_vikas",
                isVerified = true,
                content = "🌾 Today's Mandi Market Rates (Ballia District):\n• Wheat (Sharbati): ₹2,420/qtl\n• Mustard: ₹5,650/qtl\n• Potato: ₹1,180/qtl\n\nRain forecast predicts light showers over the next 48 hours. Secure stored harvest accordingly. #MandiUpdate #KisanMitra",
                timestamp = now - (2 * 60 * 60 * 1000), // 2 hours ago
                category = "General",
                likesCount = 94,
                repostsCount = 41,
                repliesCount = 8,
                viewsCount = 980,
                isLiked = false,
                isReposted = false
            ),
            PostEntity(
                id = "post_4",
                authorId = "user_pradhan",
                authorName = "Brij Bhushan Dubey",
                authorHandle = "pradhan_ji",
                isVerified = true,
                content = "The rejuvenation of the ancient village lake (Bada Pokhara) is 80% complete. Rainwater harvesting channels have been cleared. Let us maintain cleanliness and plant native trees along the embankment this Sunday. 🌱 #PanchayatClean #JalShakti",
                timestamp = now - (5 * 60 * 60 * 1000),
                category = "Civic Notice",
                likesCount = 310,
                repostsCount = 65,
                repliesCount = 22,
                viewsCount = 2780,
                isLiked = true,
                isReposted = false
            ),
            PostEntity(
                id = "post_5",
                authorId = "user_me",
                authorName = "Aman Verma",
                authorHandle = "aman_verma",
                isVerified = true,
                content = "Just tested the new solar-powered street lamps in West Tola. Remarkable illumination along the farm pathway! Women going to the evening cooperative can now travel safely. Technology empowering rural livelihoods! 💡 #Panchayat",
                timestamp = now - (12 * 60 * 60 * 1000),
                category = "Development",
                likesCount = 78,
                repostsCount = 12,
                repliesCount = 6,
                viewsCount = 650,
                isLiked = false,
                isReposted = false
            )
        )
    }

    fun getInitialReplies(): List<ReplyEntity> {
        val now = System.currentTimeMillis()
        return listOf(
            ReplyEntity(
                id = "reply_1",
                postId = "post_1",
                authorId = "user_secretary",
                authorName = "Sachiv Ji (Abhishek)",
                authorHandle = "sachiv_phulera",
                isVerified = true,
                content = "All departmental documents and budget proposals for the street lights are kept ready for review at the Bhawan.",
                timestamp = now - (10 * 60 * 1000),
                likesCount = 15,
                isLiked = false
            ),
            ReplyEntity(
                id = "reply_2",
                postId = "post_1",
                authorId = "user_me",
                authorName = "Aman Verma",
                authorHandle = "aman_verma",
                isVerified = true,
                content = "Could we also raise the proposal for a dedicated reading room in the Panchayat library during this session?",
                timestamp = now - (8 * 60 * 1000),
                likesCount = 24,
                isLiked = true
            ),
            ReplyEntity(
                id = "reply_3",
                postId = "post_2",
                authorId = "user_sarpanch",
                authorName = "Manju Devi",
                authorHandle = "sarpanch_phulera",
                isVerified = true,
                content = "Special free computer literacy sessions for village girls will start every weekday from 4 PM onwards.",
                timestamp = now - (30 * 60 * 1000),
                likesCount = 42,
                isLiked = true
            )
        )
    }

    fun getInitialNotifications(): List<NotificationEntity> {
        val now = System.currentTimeMillis()
        return listOf(
            NotificationEntity(
                id = "notif_1",
                type = "CIVIC_NOTICE",
                actorName = "Manju Devi (Sarpanch)",
                actorHandle = "sarpanch_phulera",
                message = "posted a civic announcement: Upcoming Gram Sabha meeting on Saturday at 10 AM.",
                targetPostSnippet = "Approval of 45 new solar street lights...",
                postId = "post_1",
                timestamp = now - (15 * 60 * 1000),
                isRead = false
            ),
            NotificationEntity(
                id = "notif_2",
                type = "LIKE",
                actorName = "Sachiv Ji (Abhishek)",
                actorHandle = "sachiv_phulera",
                message = "liked your Panch",
                targetPostSnippet = "Just tested the new solar-powered street lamps in West Tola...",
                postId = "post_5",
                timestamp = now - (25 * 60 * 1000),
                isRead = false
            ),
            NotificationEntity(
                id = "notif_3",
                type = "REPOST",
                actorName = "Gram Krishi Kendra",
                actorHandle = "krishi_vikas",
                message = "reposted your Panch",
                targetPostSnippet = "Just tested the new solar-powered street lamps in West Tola...",
                postId = "post_5",
                timestamp = now - (2 * 60 * 60 * 1000),
                isRead = true
            ),
            NotificationEntity(
                id = "notif_4",
                type = "REPLY",
                actorName = "Manju Devi",
                actorHandle = "sarpanch_phulera",
                message = "replied to your comment in Gram Sabha notice",
                targetPostSnippet = "Special free computer literacy sessions...",
                postId = "post_1",
                timestamp = now - (4 * 60 * 60 * 1000),
                isRead = true
            )
        )
    }

    val trendingTopics = listOf(
        TrendingTag("Governance · Trending", "#GramSabha", "1.4K Panches"),
        TrendingTag("Infrastructure · Trending", "#SolarVillage", "890 Panches"),
        TrendingTag("Technology · Trending", "#DigitalPanchayat", "2.8K Panches"),
        TrendingTag("Agriculture · Ballia District", "#MandiRates", "1.1K Panches"),
        TrendingTag("Community · Phulera", "#PanchayatElections", "3.4K Panches"),
        TrendingTag("Civic · Swachhata", "#CleanWaterMission", "650 Panches")
    )
}

package com.habittracker.ml.data.templates

import com.habittracker.ml.data.local.entities.HabitTemplate

object DefaultTemplates {

    val allTemplates = listOf(
        // Health & Fitness
        HabitTemplate(
            id = 1,
            name = "Drink 8 Glasses of Water",
            description = "Stay hydrated throughout the day",
            category = "Health",
            icon = "💧",
            defaultFrequency = "daily",
            tags = "health,hydration,wellness"
        ),
        HabitTemplate(
            id = 2,
            name = "Exercise 30 Minutes",
            description = "Daily physical activity",
            category = "Health",
            icon = "💪",
            defaultFrequency = "daily",
            defaultReminderTime = "07:00",
            tags = "fitness,exercise,health"
        ),
        HabitTemplate(
            id = 3,
            name = "Morning Yoga",
            description = "Start your day with stretching",
            category = "Health",
            icon = "🧘",
            defaultFrequency = "daily",
            defaultReminderTime = "06:30",
            tags = "yoga,mindfulness,flexibility"
        ),
        HabitTemplate(
            id = 4,
            name = "Sleep 8 Hours",
            description = "Get quality rest every night",
            category = "Health",
            icon = "😴",
            defaultFrequency = "daily",
            defaultReminderTime = "22:00",
            tags = "sleep,rest,recovery"
        ),
        HabitTemplate(
            id = 5,
            name = "Take Vitamins",
            description = "Daily vitamin supplement",
            category = "Health",
            icon = "💊",
            defaultFrequency = "daily",
            defaultReminderTime = "08:00",
            tags = "supplements,health,nutrition"
        ),
        HabitTemplate(
            id = 6,
            name = "10,000 Steps",
            description = "Walk 10,000 steps daily",
            category = "Health",
            icon = "🚶",
            defaultFrequency = "daily",
            tags = "walking,cardio,fitness"
        ),

        // Mindfulness & Mental Health
        HabitTemplate(
            id = 7,
            name = "Meditate 10 Minutes",
            description = "Daily meditation practice",
            category = "Mindfulness",
            icon = "🧘",
            defaultFrequency = "daily",
            defaultReminderTime = "07:00",
            tags = "meditation,mindfulness,peace"
        ),
        HabitTemplate(
            id = 8,
            name = "Gratitude Journal",
            description = "Write 3 things you're grateful for",
            category = "Mindfulness",
            icon = "🙏",
            defaultFrequency = "daily",
            defaultReminderTime = "21:00",
            tags = "gratitude,journaling,positivity"
        ),
        HabitTemplate(
            id = 9,
            name = "Deep Breathing",
            description = "5 minutes of breathing exercises",
            category = "Mindfulness",
            icon = "🌬️",
            defaultFrequency = "daily",
            tags = "breathing,relaxation,stress-relief"
        ),
        HabitTemplate(
            id = 10,
            name = "No Phone Before Bed",
            description = "Digital detox 1 hour before sleep",
            category = "Mindfulness",
            icon = "📵",
            defaultFrequency = "daily",
            defaultReminderTime = "21:00",
            tags = "digital-detox,sleep-hygiene,mindfulness"
        ),

        // Learning & Productivity
        HabitTemplate(
            id = 11,
            name = "Read 30 Minutes",
            description = "Daily reading habit",
            category = "Learning",
            icon = "📚",
            defaultFrequency = "daily",
            defaultReminderTime = "20:00",
            tags = "reading,learning,knowledge"
        ),
        HabitTemplate(
            id = 12,
            name = "Learn New Skill",
            description = "Practice a new skill for 20 minutes",
            category = "Learning",
            icon = "🎯",
            defaultFrequency = "daily",
            tags = "learning,skill-building,growth"
        ),
        HabitTemplate(
            id = 13,
            name = "Study Language",
            description = "Practice foreign language",
            category = "Learning",
            icon = "🗣️",
            defaultFrequency = "daily",
            defaultReminderTime = "19:00",
            tags = "language,learning,communication"
        ),
        HabitTemplate(
            id = 14,
            name = "Plan Tomorrow",
            description = "Review and plan next day",
            category = "Productivity",
            icon = "📝",
            defaultFrequency = "daily",
            defaultReminderTime = "21:00",
            tags = "planning,organization,productivity"
        ),
        HabitTemplate(
            id = 15,
            name = "Review Weekly Goals",
            description = "Check progress on weekly goals",
            category = "Productivity",
            icon = "🎯",
            defaultFrequency = "weekly",
            tags = "goals,review,planning"
        ),
        HabitTemplate(
            id = 16,
            name = "Clear Email Inbox",
            description = "Achieve inbox zero",
            category = "Productivity",
            icon = "📧",
            defaultFrequency = "daily",
            tags = "email,organization,productivity"
        ),

        // Social & Relationships
        HabitTemplate(
            id = 17,
            name = "Call Family/Friend",
            description = "Stay connected with loved ones",
            category = "Social",
            icon = "📞",
            defaultFrequency = "daily",
            tags = "social,relationships,connection"
        ),
        HabitTemplate(
            id = 18,
            name = "Quality Time with Partner",
            description = "Spend meaningful time together",
            category = "Social",
            icon = "❤️",
            defaultFrequency = "daily",
            defaultReminderTime = "19:00",
            tags = "relationship,love,connection"
        ),
        HabitTemplate(
            id = 19,
            name = "Random Act of Kindness",
            description = "Do something nice for someone",
            category = "Social",
            icon = "🤝",
            defaultFrequency = "daily",
            tags = "kindness,giving,compassion"
        ),

        // Finance
        HabitTemplate(
            id = 20,
            name = "Track Expenses",
            description = "Log daily spending",
            category = "Finance",
            icon = "💰",
            defaultFrequency = "daily",
            defaultReminderTime = "20:00",
            tags = "finance,budgeting,money"
        ),
        HabitTemplate(
            id = 21,
            name = "Review Budget",
            description = "Check monthly budget status",
            category = "Finance",
            icon = "💵",
            defaultFrequency = "weekly",
            tags = "finance,budgeting,planning"
        ),
        HabitTemplate(
            id = 22,
            name = "Save Money",
            description = "Put aside savings",
            category = "Finance",
            icon = "🏦",
            defaultFrequency = "daily",
            tags = "savings,finance,goals"
        ),

        // Creativity
        HabitTemplate(
            id = 23,
            name = "Write 500 Words",
            description = "Daily writing practice",
            category = "Creativity",
            icon = "✍️",
            defaultFrequency = "daily",
            tags = "writing,creativity,expression"
        ),
        HabitTemplate(
            id = 24,
            name = "Draw/Sketch",
            description = "Practice art for 20 minutes",
            category = "Creativity",
            icon = "🎨",
            defaultFrequency = "daily",
            tags = "art,drawing,creativity"
        ),
        HabitTemplate(
            id = 25,
            name = "Play Music",
            description = "Practice instrument",
            category = "Creativity",
            icon = "🎵",
            defaultFrequency = "daily",
            defaultReminderTime = "18:00",
            tags = "music,practice,creativity"
        ),

        // Home & Environment
        HabitTemplate(
            id = 26,
            name = "Make Bed",
            description = "Start day with a clean bed",
            category = "Home",
            icon = "🛏️",
            defaultFrequency = "daily",
            defaultReminderTime = "07:00",
            tags = "organization,home,routine"
        ),
        HabitTemplate(
            id = 27,
            name = "Clean 15 Minutes",
            description = "Daily tidying routine",
            category = "Home",
            icon = "🧹",
            defaultFrequency = "daily",
            tags = "cleaning,organization,home"
        ),
        HabitTemplate(
            id = 28,
            name = "Water Plants",
            description = "Care for houseplants",
            category = "Home",
            icon = "🪴",
            defaultFrequency = "weekly",
            tags = "plants,home,care"
        ),

        // Self-Care
        HabitTemplate(
            id = 29,
            name = "Skincare Routine",
            description = "Morning and evening skincare",
            category = "Self-Care",
            icon = "🧴",
            defaultFrequency = "daily",
            tags = "skincare,self-care,beauty"
        ),
        HabitTemplate(
            id = 30,
            name = "Take Break from Work",
            description = "Step away every 2 hours",
            category = "Self-Care",
            icon = "☕",
            defaultFrequency = "daily",
            tags = "breaks,rest,work-life-balance"
        )
    )

    fun getByCategory(category: String): List<HabitTemplate> {
        return allTemplates.filter { it.category == category }
    }

    fun search(query: String): List<HabitTemplate> {
        val lowerQuery = query.lowercase()
        return allTemplates.filter { template ->
            template.name.lowercase().contains(lowerQuery) ||
                    template.description.lowercase().contains(lowerQuery) ||
                    template.tags.lowercase().contains(lowerQuery)
        }
    }

    fun getPopular(limit: Int = 10): List<HabitTemplate> {
        return allTemplates.take(limit)
    }
}
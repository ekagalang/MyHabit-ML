# MyHabit Dummy Data - Enhanced ML Testing Dataset

## Overview
This enhanced dummy dataset has been generated to provide comprehensive, realistic data for testing machine learning features in the MyHabit app.

## Data Specifications

### Scale
- **Total Check-ins**: 762 entries
- **Date Range**: November 1, 2024 - January 15, 2025 (~2.5 months)
- **Total Habits**: 15 unique habits
- **File Size**: ~195KB

### Distribution by Habit
| Habit ID | Habit Name | Check-ins | Completion Rate |
|----------|------------|-----------|-----------------|
| 1 | Morning Workout | 49 | 85% |
| 2 | Drink 8 Glasses of Water | 75 | 95% |
| 3 | Meditation | 72 | 90% |
| 4 | Read for 30 Minutes | 43 | 75% |
| 5 | Deep Work Session | 50 | 80% |
| 6 | Call Family | 27 | 70% |
| 7 | Journaling | 71 | 88% |
| 8 | Learn Programming | 43 | 78% |
| 9 | Healthy Breakfast | 75 | 92% |
| 10 | No Social Media Before Bed | 69 | 85% |
| 11 | Evening Walk | 36 | 65% |
| 12 | Practice Guitar | 32 | 60% |
| 13 | Clean Workspace | 28 | 70% |
| 14 | Gratitude Practice | 72 | 93% |
| 15 | Yoga Session | 20 | 55% (inactive) |

## Data Quality Features

### 1. Timing Patterns
- **Late Check-ins**: 106 (13.9%)
- **On-time**: 656 (86.1%)
- **Time Variance**: ±15 minutes for most habits
- **Late Range**: 5-120 minutes when late

### 2. Mood Distribution
- **Very Happy**: 165 entries (21.7%)
- **Happy**: 246 entries (32.3%)
- **Neutral**: 270 entries (35.4%)
- **Sad**: 81 entries (10.6%)

### 3. Contextual Data
- **Weather Types**: 5 varieties (sunny, cloudy, rainy, cold, hot)
- **Locations**: 5 types (home, work, gym, outdoor, park)
- **Energy Levels**: 1-5 scale with realistic distributions
- **Stress Levels**: 1-5 scale with realistic distributions
- **Notes**: 29.5% of check-ins include notes

## ML-Ready Patterns

### 1. Temporal Patterns
- **Day-of-week effects**: Weekend boost for personal habits
- **Time-of-day patterns**: Morning habits have higher energy levels
- **Progression over time**: Slight improvement in adherence over the 2.5 months

### 2. Weather Correlations
- Outdoor activities (Morning Workout, Evening Walk) show 20% lower completion rate in rainy weather
- Indoor habits remain stable across weather conditions

### 3. Mood-Energy Correlations
- Morning workouts correlate with positive moods (80%+ happy/very_happy)
- Meditation shows very high positive mood correlation (90%+)
- Deep work and programming show more mood variance based on productivity

### 4. Stress-Performance Patterns
- High stress (4-5) correlates with neutral/sad moods
- Low stress (1-2) correlates with positive moods
- Mindfulness habits (meditation, journaling) show lower stress levels

### 5. Location Patterns
- Gym visits for habit #1 show higher energy levels
- Work location for habit #5 (Deep Work) has varied stress levels
- Home location is most common (70%+ of check-ins)

### 6. Realistic Failures
- Not all target days are completed (varies by habit)
- Some habits show declining adherence (guitar, yoga)
- Some habits maintain high consistency (water, gratitude, meditation)

## Usage for ML Testing

### Recommended Test Cases

1. **Prediction Models**
   - Predict completion probability based on day, weather, and mood history
   - Forecast next completion time based on past patterns
   - Identify high-risk failure days

2. **Pattern Recognition**
   - Detect habit streaks and breaks
   - Identify optimal completion times
   - Find mood-habit correlations

3. **Recommendation Engine**
   - Suggest best times for new habits
   - Recommend habit stacking opportunities
   - Identify context-based triggers (weather, location)

4. **Anomaly Detection**
   - Flag unusual patterns (e.g., late completions spike)
   - Detect habit deterioration early
   - Identify stress/mood warning signs

5. **Clustering & Segmentation**
   - Group habits by completion patterns
   - Identify habit archetypes (daily vs. periodic)
   - Segment users by adherence levels

## Data Generation Methodology

The data was generated using a Python script (`generate_dummy_data.py`) with the following logic:

1. **Base Completion Rates**: Each habit assigned realistic adherence probability
2. **Contextual Modifiers**:
   - Weekend boost: +10% for personal growth habits
   - Weather penalty: -20% for outdoor habits in rain
   - Time progression: Gradual improvement over time
3. **Realistic Variance**: Random factors add natural variation
4. **Correlation Logic**: Mood, energy, and stress levels are interconnected
5. **Note Generation**: 30% of check-ins get contextual notes

## File Format

JSON structure:
```json
{
  "exportDate": <timestamp>,
  "version": "1.0",
  "totalHabits": 15,
  "totalCheckIns": 762,
  "dateRangeStart": "2024-11-01",
  "dateRangeEnd": "2025-01-15",
  "habits": [...],
  "checkIns": [...]
}
```

## Future Enhancements

Potential areas for expansion:
- [ ] Extend to 6-12 months of data
- [ ] Add more habit types (e.g., social, financial)
- [ ] Include seasonal variations
- [ ] Add habit modifications/edits over time
- [ ] Include goal adjustments
- [ ] Add social sharing events
- [ ] Include streak milestone celebrations

---

Generated: January 29, 2026
Last Updated: January 29, 2026

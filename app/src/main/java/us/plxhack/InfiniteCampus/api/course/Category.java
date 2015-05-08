package us.plxhack.InfiniteCampus.api.course;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import nu.xom.Element;

public class Category
{
    private String name;

    private float percentage;
    private float earnedPoints, totalPoints = 0;
    private float weight;
    private String letterGrade;

    private List<Activity> activities;

    public Category(Element groupElement, String courseName)
    {
        name = groupElement.getAttributeValue("name");
        percentage = Float.valueOf(groupElement.getAttributeValue("percentage"));
        earnedPoints = Float.valueOf(groupElement.getAttributeValue("pointsEarned"));
        totalPoints = Float.valueOf(groupElement.getAttributeValue("totalPointsPossible"));
        weight = Float.valueOf(groupElement.getAttributeValue("weight"));
        String gradeLetter = groupElement.getAttributeValue("letterGrade");
        if (gradeLetter != null)
            letterGrade = gradeLetter;
        else
            letterGrade = "N/A";

        Element activities = groupElement.getFirstChildElement("activities");

        this.activities = new ArrayList<>();
        for (int k = 0; k < activities.getChildCount(); ++k) {
            Element activityElement = activities.getChildElements().get(k);

            if(activityElement != null)
            {
                this.activities.add(new Activity(activityElement, courseName));
            }
        }

        sort();

    }

    public Category(JSONObject jsonCategory) throws JSONException
    {
        name = jsonCategory.getString("name");

        percentage = (float)jsonCategory.getDouble("percent");
        earnedPoints = jsonCategory.getInt("earnedPoints");
        totalPoints = jsonCategory.getInt("totalPoints");
        weight = jsonCategory.getInt("weight");
        letterGrade = jsonCategory.getString("letterGrade");

        JSONArray jsonActivities = jsonCategory.getJSONArray("activities");
        activities = new ArrayList<>();
        for(int l = 0; l < jsonActivities.length(); l++)
        {
            activities.add(new Activity(jsonActivities.getJSONObject(l)));
        }
    }

    public Category( String name )
    {
        this.name = name;
        activities = new ArrayList<Activity>();
    }

    public void sort()
    {
        ArrayList<Activity> tempList = new ArrayList<Activity>();

        for (int i=0;i < activities.size();++i)
        {
            if (tempList.size() == 0)
            {
                tempList.add( activities.get(i) );
            }
            else
            {
                int placement = -1;

                for (int j=0;j < tempList.size();++j)
                {
                    if (activities.get(i).getName().compareTo(tempList.get(j).getName()) < 0 && placement == -1)
                    {
                        placement = j;
                        break;
                    }
                }

                if (placement != -1)
                    tempList.add( placement, activities.get(i) );
                else
                    tempList.add( activities.get(i) );
            }
        }

        activities = tempList;
    }

    public String getName()
    {
        return name;
    }
    public String getLetterGrade()
    {
        return letterGrade;
    }
    public float getWeight()
    {
        return weight;
    }
    public float getPercentage()
    {
        return percentage;
    }
    public float getEarnedPoints()
    {
        return earnedPoints;
    }
    public float getTotalPoints()
    {
        return totalPoints;
    }
    public List<Activity> getActivites()
    {
        return activities;
    }
}

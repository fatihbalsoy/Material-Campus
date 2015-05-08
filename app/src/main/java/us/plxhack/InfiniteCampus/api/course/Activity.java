package us.plxhack.InfiniteCampus.api.course;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

import nu.xom.Element;
import us.plxhack.InfiniteCampus.api.InfiniteCampusApi;

public class Activity
{
    private String name;

    private float percentage;
    private float totalPoints;
    private String earnedPoints = "";
    private String letterGrade;
    private boolean missing;
    private String dueDate;
    private String id;
    private String className;

    public Activity(Element activityElement, String courseName)
    {
        name = activityElement.getAttributeValue("name");
        percentage = Float.valueOf(activityElement.getAttributeValue("percentage"));
        earnedPoints = activityElement.getAttributeValue("score");
        totalPoints = Float.valueOf(activityElement.getAttributeValue("weightedTotalPoints"));
        dueDate = activityElement.getAttributeValue("dueDate");
        missing = Boolean.valueOf(activityElement.getAttributeValue("missing"));
        id = activityElement.getAttributeValue("activityID");
        className = courseName;
        String letterGrade = activityElement.getAttributeValue("letterGrade");
        if (letterGrade != null)
            this.letterGrade = letterGrade;
        else
            this.letterGrade = "N/A";

        try
        {
            JSONObject jsonActivity = InfiniteCampusApi.getInstance().findAssignmentJSON(InfiniteCampusApi.getInstance().getSaveFile(), id);
            boolean changed = false;
            if(jsonActivity != null)
            {
                if(getEarnedPoints() != null)
                {
                    if(!getEarnedPoints().equals(jsonActivity.getString("earnedPoints")) || jsonActivity.getString("earnedPoints") == null)
                    {
                        changed = true;
                    }
                }
                if(isMissing() != jsonActivity.getBoolean("missing"))
                {
                    changed = true;
                }
            }
            else
            {
                changed = true;
            }

            if(changed)
            {
                InfiniteCampusApi.getInstance().getUpdated().add(this);
            }
        } catch (JSONException e1)
        {
            e1.printStackTrace();
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public Activity(JSONObject jsonActivity) throws JSONException
    {
        name = jsonActivity.getString("name");

        percentage = (float) jsonActivity.getDouble("percent");
        earnedPoints = jsonActivity.getString("earnedPoints");
        totalPoints = jsonActivity.getInt("totalPoints");
        letterGrade = jsonActivity.getString("letterGrade");
        id = jsonActivity.getString("id");
        missing = jsonActivity.getBoolean("missing");
        dueDate = jsonActivity.getString("dueDate");
        className = jsonActivity.getString("className");
    }

    public Activity( String name )
    {
        this.name = name;
    }

    public String getName()
    {
        return name;
    }
    public String getLetterGrade()
    {
        return letterGrade;
    }
    public float getPercentage()
    {
        return percentage;
    }
    public String getEarnedPoints()
    {
        return earnedPoints;
    }
    public float getTotalPoints()
    {
        return totalPoints;
    }
    public boolean isMissing()
    {
        return missing;
    }
    public String getDueDate()
    {
        return dueDate;
    }
    public String getId()
    {
        return id;
    }
    public String getClassName()
    {
        return className;
    }
}

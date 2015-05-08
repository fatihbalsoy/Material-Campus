package us.plxhack.InfiniteCampus.api.course;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import nu.xom.Element;

public class Task
{
    private String name;
    private float weight;
    private String termName;

    private float percentage;
    private float earnedPoints, totalPoints;
    private String letterGrade;

    private List<Category> gradeCategories;

    public Task(Element task, Element finalTaskGroups, String courseName)
    {
        name = task.getAttributeValue("name");
        termName = task.getAttributeValue("termName");
        weight = Float.valueOf(task.getAttributeValue("weight"));
        percentage = Float.valueOf(task.getAttributeValue("percentage"));
        letterGrade = task.getAttributeValue("letterGrade");

        gradeCategories = new ArrayList<>();
        for (int j = 0; j < finalTaskGroups.getChildCount(); ++j)
        {
            Element groupElement = finalTaskGroups.getChildElements().get(j);

            if(groupElement != null)
            {
                gradeCategories.add(new Category(groupElement, courseName));
            }
        }
    }
    
    public Task(JSONObject jsonTask) throws JSONException
    {
        name = jsonTask.getString("name");

        termName = jsonTask.getString("termName");
        percentage = (float)jsonTask.getDouble("percent");
        earnedPoints = jsonTask.getInt("earnedPoints");
        totalPoints = jsonTask.getInt("totalPoints");
        weight = jsonTask.getInt("weight");
        letterGrade = jsonTask.getString("letterGrade");

        JSONArray jsonCategories = jsonTask.getJSONArray("categories");
        gradeCategories = new ArrayList<>();
        for(int k = 0; k < jsonCategories.length(); k++)
        {
            gradeCategories.add(new Category(jsonCategories.getJSONObject(k)));
        }
    }
    
    public Task( String name )
    {
        this.name = name;
        gradeCategories = new ArrayList<Category>();
    }

    public String getName()
    {
        return name;
    }
    public String getTermName()
    {
        return termName;
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
    public List<Category> getCategories()
    {
        return gradeCategories;
    }
}

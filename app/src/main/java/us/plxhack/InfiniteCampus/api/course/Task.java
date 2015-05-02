package us.plxhack.InfiniteCampus.api.course;

import java.util.ArrayList;
import java.util.List;

public class Task
{
    public String name;
    public float weight;
    public String termName;

    public float percentage;
    public float earnedPoints, totalPoints;
    public String letterGrade;

    public List<Category> gradeCategories;

    public Task( String name )
    {
        this.name = name;
        gradeCategories = new ArrayList<Category>();
    }

    public void addCategory( Category c )
    {
        gradeCategories.add( c );
    }
}

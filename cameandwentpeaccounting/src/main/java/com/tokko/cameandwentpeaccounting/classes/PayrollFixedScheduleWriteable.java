package com.tokko.cameandwentpeaccounting.classes;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Schema fragment(s) for this class:
 * <pre>
 * &lt;xs:complexType xmlns:xs="http://www.w3.org/2001/XMLSchema"
 * name="payroll-fixed-schedule-writeable">
 *   &lt;xs:sequence>
 *     &lt;xs:element type="xs:string" name="name"/>
 *     &lt;xs:element type="xs:string" name="description"/>
 *     &lt;xs:element type="xs:decimal" name="hours-per-week" minOccurs="0"/>
 *     &lt;xs:element type="xs:decimal" name="level-of-employment" minOccurs="0"/>
 *     &lt;xs:element name="intervals" minOccurs="0">
 *       &lt;!-- Reference to inner class Intervals -->
 *     &lt;/xs:element>
 *   &lt;/xs:sequence>
 * &lt;/xs:complexType>
 * </pre>
 */
public class PayrollFixedScheduleWriteable{
    private String name;
    private String description;
    private BigDecimal hoursPerWeek;
    private BigDecimal levelOfEmployment;
    private Intervals intervals;

    /**
     * Get the 'name' element value.
     *
     * @return value
     */
    public String getName(){
        return name;
    }

    /**
     * Set the 'name' element value.
     *
     * @param name
     */
    public void setName(String name){
        this.name = name;
    }

    /**
     * Get the 'description' element value.
     *
     * @return value
     */
    public String getDescription(){
        return description;
    }

    /**
     * Set the 'description' element value.
     *
     * @param description
     */
    public void setDescription(String description){
        this.description = description;
    }

    /**
     * Get the 'hours-per-week' element value.
     *
     * @return value
     */
    public BigDecimal getHoursPerWeek(){
        return hoursPerWeek;
    }

    /**
     * Set the 'hours-per-week' element value.
     *
     * @param hoursPerWeek
     */
    public void setHoursPerWeek(BigDecimal hoursPerWeek){
        this.hoursPerWeek = hoursPerWeek;
    }

    /**
     * Get the 'level-of-employment' element value.
     *
     * @return value
     */
    public BigDecimal getLevelOfEmployment(){
        return levelOfEmployment;
    }

    /**
     * Set the 'level-of-employment' element value.
     *
     * @param levelOfEmployment
     */
    public void setLevelOfEmployment(BigDecimal levelOfEmployment){
        this.levelOfEmployment = levelOfEmployment;
    }

    /**
     * Get the 'intervals' element value.
     *
     * @return value
     */
    public Intervals getIntervals(){
        return intervals;
    }

    /**
     * Set the 'intervals' element value.
     *
     * @param intervals
     */
    public void setIntervals(Intervals intervals){
        this.intervals = intervals;
    }

    /**
     * Schema fragment(s) for this class:
     * <pre>
     * &lt;xs:element xmlns:xs="http://www.w3.org/2001/XMLSchema" name="intervals" minOccurs="0">
     *   &lt;xs:complexType>
     *     &lt;xs:sequence>
     *       &lt;xs:element type="payroll-fixed-schedule-interval" name="interval" minOccurs="0"
     *       maxOccurs="unbounded"/>
     *     &lt;/xs:sequence>
     *   &lt;/xs:complexType>
     * &lt;/xs:element>
     * </pre>
     */
    public static class Intervals{
        private List<PayrollFixedScheduleInterval> intervalList = new
                ArrayList<PayrollFixedScheduleInterval>();

        /**
         * Get the list of 'interval' element items.
         *
         * @return list
         */
        public List<PayrollFixedScheduleInterval> getIntervalList(){
            return intervalList;
        }

        /**
         * Set the list of 'interval' element items.
         *
         * @param list
         */
        public void setIntervalList(List<PayrollFixedScheduleInterval> list){
            intervalList = list;
        }
    }
}
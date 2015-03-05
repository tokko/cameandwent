package com.tokko.cameandwent.cameandwent;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;

import junit.framework.TestCase;

import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.DurationFieldType;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowContentResolver;
import org.robolectric.shadows.ShadowPreferenceManager;

@Config(emulateSdk = 18, manifest = "app/src/main/AndroidManifest.xml")
@RunWith(RobolectricTestRunner.class)
public class CameAndWentProviderRoboTests extends TestCase{

    private CameAndWentProvider mProvider;
    private ContentResolver mContentResolver;
    private ShadowContentResolver mShadowContentResolver;

    @Before
    public void setup(){
        mProvider = new CameAndWentProvider();
        mContentResolver = Robolectric.application.getContentResolver();
        mShadowContentResolver = Robolectric.shadowOf(mContentResolver);
        mProvider.onCreate();
        ShadowContentResolver.registerProvider(CameAndWentProvider.AUTHORITY, mProvider);
        SharedPreferences sharedPreferences = ShadowPreferenceManager.getDefaultSharedPreferences(Robolectric.application.getApplicationContext());
        sharedPreferences.edit().clear()
                .putBoolean("breaks_enabled", true)
                .putString("average_break_start", "12:00")
                .putString("average_break_duration", "00:30")
                .apply();
        mShadowContentResolver.call(CameAndWentProvider.URI_GET_MONTHLY_SUMMARY, CameAndWentProvider.SEED_METHOD, null, null);
    }

   @Test
   public void testMonthlySummaryView_Created(){
        Cursor c = mContentResolver.query(CameAndWentProvider.URI_GET_MONTHLY_SUMMARY, null, null, null, null);
        assertTrue(c.getCount() > 0);
        String[] names = c.getColumnNames();
        assertEquals(3, names.length);
        assertTrue(contains(names, CameAndWentProvider.ID, CameAndWentProvider.WEEK_OF_YEAR, CameAndWentProvider.DURATION));
        c.close();
    }
    @Test
    public void testMonthlySummaryView_CorrectData(){
        Cursor c = mContentResolver.query(CameAndWentProvider.URI_GET_MONTHLY_SUMMARY, null, null, null, CameAndWentProvider.WEEK_OF_YEAR);
        assertEquals(CameAndWentProvider.WEEKS_BACK, c.getCount());
        long duration = DateTimeConstants.MILLIS_PER_HOUR*40;
        int[] weeks = new int[52];
        for (c.moveToFirst(); !c.isLast(); c.moveToNext()){
            weeks[c.getInt(c.getColumnIndex(CameAndWentProvider.WEEK_OF_YEAR))]++;
            assertEquals(duration, c.getLong(c.getColumnIndex(CameAndWentProvider.DURATION)));
        }
        c.close();
        for (int week : weeks)
            assertFalse(weeks[week] > 1);
    }
    @SafeVarargs
    private final <T> boolean contains(T[] arr, T... elems){
        for(T t : elems)
            if(contains(arr, t)) return true;
        return false;
    }

    private <T> boolean contains(T[] arr, T elem){
        for(T t : arr)
            if(t.equals(elem))
                return true;
        return false;
    }


    @Test
    public void testCame(){
        int pre = mContentResolver.query(CameAndWentProvider.URI_GET_DETAILS, null, null, null, null).getCount();
        ContentValues cv = new ContentValues();
        cv.put(CameAndWentProvider.CAME, System.currentTimeMillis());
        Uri postInsertUri = mContentResolver.insert(CameAndWentProvider.URI_CAME, cv);
        long id = ContentUris.parseId(postInsertUri);
        assertTrue(-1 != id);
        Cursor post = mContentResolver.query(CameAndWentProvider.URI_GET_DETAILS, null, null, null, null);
        assertEquals(pre+1, post.getCount()); //new entry + break
        post.close();
    }

    @Test
    public void getWeeks_FetchesAllWeeks(){
        DateTime dt = CameAndWentProvider.getSeedDateTime();
        Cursor c = mContentResolver.query(CameAndWentProvider.URI_GET_GET_WEEKS, null, null, null, CameAndWentProvider.WEEK_OF_YEAR);
        for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext(), dt = dt.withFieldAdded(DurationFieldType.weeks(), 1))
            assertEquals(dt.getWeekOfWeekyear(), c.getInt(c.getColumnIndex(CameAndWentProvider.WEEK_OF_YEAR)));
        c.close();
    }

    @Test
    public void getMonths_FetchesAllMonths(){
        DateTime dt = CameAndWentProvider.getSeedDateTime();
        Cursor c = mContentResolver.query(CameAndWentProvider.URI_GET_GET_MONTHS, null, null, null, CameAndWentProvider.MONTH_OF_YEAR);
        for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext(), dt = dt.withFieldAdded(DurationFieldType.months(), 1))
            assertEquals(dt.getMonthOfYear(), c.getInt(c.getColumnIndex(CameAndWentProvider.MONTH_OF_YEAR)));
        c.close();
    }

    @Test
    public void testWent(){
        ContentValues cv = new ContentValues();
        cv.put(CameAndWentProvider.CAME, System.currentTimeMillis());
        Uri postInsertUri = mContentResolver.insert(CameAndWentProvider.URI_CAME, cv);
        long id = ContentUris.parseId(postInsertUri);
        cv = new ContentValues();
        cv.put(CameAndWentProvider.WENT, System.currentTimeMillis());
        int updated = mContentResolver.update(CameAndWentProvider.URI_WENT, cv, String.format("%s=?", CameAndWentProvider.ID), new String[]{String.valueOf(id)});
        assertEquals(1, updated);
    }

    @Test
    public void testGetEntries(){
        Cursor c = mContentResolver.query(CameAndWentProvider.URI_GET_LOG_ENTRIES, null, null, null, null);
        long duration = TimeConverter.hoursAsLong(8);
        for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext())
             assertEquals(duration, c.getLong(c.getColumnIndex(CameAndWentProvider.DURATION)));
        c.close();
    }

    @Test
    public void testGetDetails(){
        Cursor c = mContentResolver.query(CameAndWentProvider.URI_GET_DETAILS, null, null, null, null);
        assertNotNull(c);
        assertEquals(CameAndWentProvider.SEED_ENTRIES, c.getCount());
        for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()){
            long came = c.getLong(c.getColumnIndex(CameAndWentProvider.CAME));
            long went = c.getLong(c.getColumnIndex(CameAndWentProvider.WENT));
            long date = c.getLong(c.getColumnIndex(CameAndWentProvider.DATE));
            assertTrue(came < went);
            assertEquals(date, TimeConverter.extractDate(came));
            assertEquals(date, TimeConverter.extractDate(went));
        }
        c.close();
        Cursor noBreak = mContentResolver.query(CameAndWentProvider.URI_GET_DETAILS, null, String.format("%s=?", CameAndWentProvider.ISBREAK), new String[]{String.valueOf(0)}, null);
        Cursor isBreak = mContentResolver.query(CameAndWentProvider.URI_GET_DETAILS, null, String.format("%s=?", CameAndWentProvider.ISBREAK), new String[]{String.valueOf(1)}, null);
        assertEquals(CameAndWentProvider.SEED_ENTRIES*2/3, noBreak.getCount());
        assertEquals(noBreak.getColumnCount(), noBreak.getCount()/3, isBreak.getCount());
        noBreak.close();
        isBreak.close();
    }



    @Test
    public void testDeleteDetail(){
        Cursor c = mContentResolver.query(CameAndWentProvider.URI_GET_DETAILS, null, null, null, null);
        assertEquals(CameAndWentProvider.SEED_ENTRIES, c.getCount());
        int deleted = mContentResolver.delete(CameAndWentProvider.URI_DELETE_ALL, null, null);
        assertEquals(CameAndWentProvider.SEED_ENTRIES, deleted);
    }

    @Test
    public void testUpdateEntry(){
        Cursor toEdit = mContentResolver.query(CameAndWentProvider.URI_GET_DETAILS, null, null, null, null);
        assertNotNull(toEdit);
        assertTrue(toEdit.moveToLast());
        long id = toEdit.getLong(toEdit.getColumnIndex(CameAndWentProvider.ID));
        long came = toEdit.getLong(toEdit.getColumnIndex(CameAndWentProvider.CAME));
        long date = toEdit.getLong(toEdit.getColumnIndex(CameAndWentProvider.DATE));

        long newWent = System.currentTimeMillis() + 10000;
        ContentValues cv = new ContentValues();
        cv.put(CameAndWentProvider.WENT, newWent);

        int updated = mContentResolver.update(CameAndWentProvider.URI_UPDATE_PARTICULAR_LOG_ENTRY, cv, String.format("%s=?", CameAndWentProvider.ID), new String[]{String.valueOf(id)});
        assertEquals(1, updated);

        Cursor afterUpdate = mContentResolver.query(CameAndWentProvider.URI_GET_DETAILS, null, String.format("%s=?", CameAndWentProvider.ID), new String[]{String.valueOf(id)}, null);
        assertNotNull(afterUpdate);
        assertEquals(1, afterUpdate.getCount());
        assertTrue(afterUpdate.moveToFirst());
        assertEquals(id, afterUpdate.getLong(afterUpdate.getColumnIndex(CameAndWentProvider.ID)));
        assertEquals(came, afterUpdate.getLong(afterUpdate.getColumnIndex(CameAndWentProvider.CAME)));
        assertEquals(date, afterUpdate.getLong(afterUpdate.getColumnIndex(CameAndWentProvider.DATE)));
        assertEquals(newWent, afterUpdate.getLong(afterUpdate.getColumnIndex(CameAndWentProvider.WENT)));
        toEdit.close();
        afterUpdate.close();
    }

    @Test
    public void updateEntry_ImplicitFieldsUpdated(){
        Cursor toEdit = mContentResolver.query(CameAndWentProvider.URI_GET_DETAILS, null, null, null, null);
        assertNotNull(toEdit);
        assertTrue(toEdit.moveToLast());
        long id = toEdit.getLong(toEdit.getColumnIndex(CameAndWentProvider.ID));


        DateTime dt = TimeConverter.getCurrentTime();
        dt = dt.withFieldAdded(DurationFieldType.months(), 1);

        ContentValues cv = new ContentValues();
        cv.put(CameAndWentProvider.CAME, dt.getMillis());
        cv.put(CameAndWentProvider.WENT, dt.withFieldAdded(DurationFieldType.hours(), 4).getMillis());

        int updated = mContentResolver.update(CameAndWentProvider.URI_UPDATE_PARTICULAR_LOG_ENTRY, cv, String.format("%s=?", CameAndWentProvider.ID), new String[]{String.valueOf(id)});
        assertEquals(1, updated);

        Cursor afterUpdate = mContentResolver.query(CameAndWentProvider.URI_GET_DETAILS, null, String.format("%s=?", CameAndWentProvider.ID), new String[]{String.valueOf(id)}, null);
        assertNotNull(afterUpdate);
        assertEquals(1, afterUpdate.getCount());
        assertTrue(afterUpdate.moveToFirst());
        assertEquals(id, afterUpdate.getLong(afterUpdate.getColumnIndex(CameAndWentProvider.ID)));
        assertEquals(dt.getMillis(), afterUpdate.getLong(afterUpdate.getColumnIndex(CameAndWentProvider.CAME)));
        assertEquals(dt.withFieldAdded(DurationFieldType.hours(), 4).getMillis(), afterUpdate.getLong(afterUpdate.getColumnIndex(CameAndWentProvider.WENT)));
        assertEquals(dt.getMonthOfYear(), afterUpdate.getLong(afterUpdate.getColumnIndex(CameAndWentProvider.MONTH_OF_YEAR)));
        assertEquals(dt.getWeekOfWeekyear(), afterUpdate.getLong(afterUpdate.getColumnIndex(CameAndWentProvider.WEEK_OF_YEAR)));
        assertEquals(TimeConverter.extractDate(dt.getMillis()), afterUpdate.getLong(afterUpdate.getColumnIndex(CameAndWentProvider.DATE)));
        toEdit.close();
        afterUpdate.close();
    }

    @Test
    public void testAutomaticBreaks(){
        mContentResolver.delete(CameAndWentProvider.URI_DELETE_ALL, null, null);
        long dTime = TimeConverter.getCurrentTime().withTime(12, 0, 0, 0).getMillis();
        long duration = DateTimeConstants.MILLIS_PER_HOUR/2;

        ContentValues cv = new ContentValues();
        long came = System.currentTimeMillis();
        cv.put(CameAndWentProvider.CAME, came);
        mContentResolver.insert(CameAndWentProvider.URI_CAME, cv);

        Cursor c = mContentResolver.query(CameAndWentProvider.URI_GET_DETAILS, null, String.format("%s=?", CameAndWentProvider.DATE), new String[]{String.valueOf(TimeConverter.extractDate(TimeConverter.getCurrentTime().getMillis()))},  CameAndWentProvider.CAME + " DESC");

        //TODO: se över varför ordningen på dessa verkar skifta
        assertNotNull(c);
        assertEquals(2, c.getCount());
        assertTrue(c.moveToFirst());
        assertTrue(c.getInt(c.getColumnIndex(CameAndWentProvider.ISBREAK)) == 1);
        assertEquals(dTime, c.getLong(c.getColumnIndex(CameAndWentProvider.CAME)));
        assertEquals(dTime+duration, c.getLong(c.getColumnIndex(CameAndWentProvider.WENT)));

        assertTrue(c.moveToNext());
        assertFalse(c.getInt(c.getColumnIndex(CameAndWentProvider.ISBREAK)) == 1);
        assertEquals(came, c.getLong(c.getColumnIndex(CameAndWentProvider.CAME)));

        mContentResolver.insert(CameAndWentProvider.URI_CAME, cv);
        c.close();
        c = mContentResolver.query(CameAndWentProvider.URI_GET_DETAILS, null, String.format("%s=?", CameAndWentProvider.DATE), new String[]{String.valueOf(TimeConverter.extractDate(TimeConverter.getCurrentTime().getMillis()))},  CameAndWentProvider.CAME + " DESC");
        assertNotNull(c);
        assertEquals(3, c.getCount());
        c.close();
    }
	
	@Test
	public void snapup_durationsAreProperlySnappedUp(){
		ShadowPreferenceManager.getDefaultSharedPreferences(Robolectric.application.getApplicationContext()).edit().putBoolean("use_snapup", true).apply();
		Cursor c = mContentResolver.query(CameAndWentProvider.URI_GET_LOG_ENTRIES, null, null, null, null);
		for(c.moveToFirst(); !c.isAfterLast(); c.moveToNext()){
			assertEquals(DateTimeConstants.MILLIS_PER_HOUR*8+DateTimeConstants.MILLIS_PER_MINUTE*30, c.getLong(c.getColumnIndex(CameAndWentProvider.DURATION)));
		}
        c.close();
	}
}

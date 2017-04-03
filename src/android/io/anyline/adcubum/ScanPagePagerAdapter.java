package io.anyline.adcubum;

import android.content.Context;
import android.net.Uri;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by priegler on 29/05/14.
 */

public class ScanPagePagerAdapter extends RecyclingPagerAdapter {

    private String TAG = ScanPagePagerAdapter.class.getSimpleName();

    private List<ScanPage> mPages = new ArrayList<>();
    private Context mContext;
    private LayoutInflater mLayoutInflater;
    private Lock reentrantLock = new ReentrantLock();

    public ScanPagePagerAdapter(List<ScanPage> pages, Context context, ViewPager pager) {
        mContext = context;
        mLayoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        updateList(pages, pager);
    }

    public void destroy() {
        mContext = null;
        mLayoutInflater = null;
        mPages.clear();
        mPages = null;
//        if(reentrantLock.)
//        reentrantLock.unlock();
        reentrantLock = null;
    }

    public int removeFromList(ViewPager pager, int position) {
        // ViewPager doesn't have a delete method; the closest is to set the adapter
        // again.  When doing so, it deletes all its views.  Then we can delete the view
        // from from the adapter and finally set the adapter to the pager again.  Note
        // that we set the adapter to null before removing the view from "views" - that's
        // because while ViewPager deletes all its views, it will call destroyItem which
        // will in turn cause a null pointer ref.
        reentrantLock.lock();
        int currentItem = pager.getCurrentItem();
        pager.setAdapter(null);
        mPages.remove(position);
        pager.setAdapter(this);
        if (position <= currentItem) {
            currentItem -= 1;
            pager.setCurrentItem(currentItem);
        }

        reentrantLock.unlock();
        return position;
    }

    public ArrayList<ScanPage> getParcelableData() {
        return (ArrayList<ScanPage>) this.mPages;
    }

    public void updateList(List<ScanPage> pages, ViewPager pager) {
        pager.setAdapter(null);
        reentrantLock.lock();
        mPages.clear();
        mPages.addAll(pages);
        reentrantLock.unlock();
        if (pages.size() > 0) pager.setCurrentItem(0);

        pager.setAdapter(this);
        notifyDataSetChanged();
    }


    public void addToList(ScanPage page, ViewPager pager) {
        pager.setAdapter(null);
        reentrantLock.lock();
        mPages.add(page);
        reentrantLock.unlock();
        pager.setAdapter(this);
        notifyDataSetChanged();
    }

    public void addAllToList(List<ScanPage> pages, ViewPager pager) {
        pager.setAdapter(null);
        reentrantLock.lock();
        mPages.addAll(pages);
        reentrantLock.unlock();
        pager.setAdapter(this);
        notifyDataSetChanged();
    }

    public void refresh(ViewPager viewPager) {
        int currentItem = viewPager.getCurrentItem();
        viewPager.setAdapter(null);
        viewPager.setAdapter(this);
        viewPager.setCurrentItem(currentItem);
    }


    public ScanPage getScanPage(int i) {
        reentrantLock.lock();
        if (i >= 0 && i < mPages.size()) {
            ScanPage t = mPages.get(i);
            reentrantLock.unlock();
            return t;
        } else {
            reentrantLock.unlock();
            Log.e(TAG, "getScanPage Failed");
            return null;
        }
    }

    public List<ScanPage> getAllScanPages() {
        reentrantLock.lock();
        reentrantLock.unlock();
        return mPages;
    }

    @Override
    public int getCount() {
        reentrantLock.lock();
        int size = mPages.size();
        reentrantLock.unlock();
        return size;
    }

    @Override
    public View getView(final int position, View view, ViewGroup container) {
        Log.d(TAG, "getView position: " + position);
        ViewHolder viewHolder = null;
        if (view != null) {
            viewHolder = (ViewHolder) view.getTag();
        }
        if (viewHolder == null) {
            view = mLayoutInflater.inflate(mContext.getResources().getIdentifier("scan_page_layout", "layout", "io.anyline.adcubum"), container, false);
            viewHolder = new ViewHolder();
            viewHolder.pageImageView = (ImageView) view.findViewById(mContext.getResources().getIdentifier("scanPageImageView", "id", "io.anyline.adcubum"));
            view.setTag(viewHolder);
        }

        final ScanPage page = mPages.get(position);
        File image = page.getCroppedImageFile();

        Picasso.with(mContext).load(Uri.fromFile(image)).rotate(page.getRotationInDegrees())
                .fit().centerInside().into(viewHolder.pageImageView);


        final GestureDetector gestureDetector = new GestureDetector(mContext, new SingleTapConfirm(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // on item clicked
            }
        }));


        return view;
    }

    private class SingleTapConfirm extends GestureDetector.SimpleOnGestureListener {

        private View.OnClickListener onClickListener;

        public SingleTapConfirm(View.OnClickListener onClickListener) {

            this.onClickListener = onClickListener;
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent event) {
            Log.d(TAG, "onSingleTapConfirmed: ");
            onClickListener.onClick(null);
            return true;
        }
    }

    public static class ViewHolder {
        ImageView pageImageView;

    }
}

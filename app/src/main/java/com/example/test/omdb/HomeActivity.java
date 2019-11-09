package com.example.test.omdb;

import android.app.ProgressDialog;
import android.app.SearchManager;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.View;

import com.example.test.omdb.fragments.tabs.GridRecyclerFragment;
import com.example.test.omdb.models.SearchResultModel;
import com.example.test.omdb.models.moviePoster.MovieSearchList;
import com.example.test.omdb.retrofit.ApiCall;
import com.example.test.omdb.utilities.OnLoadMoreListener;


import java.util.ArrayList;
import java.util.List;


import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeActivity extends AppCompatActivity {

    public static ArrayList<MovieSearchList.Search> movies;
    private final String TAG = "HomeActivity";
    public SearchResultModel searchResult;
    public MovieSearchList movieSearchList;
    private int pagesLoaded;
    private String latestQuery;
    private boolean reachedEnd;

    private Toolbar toolbar;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private SearchView searchView;
    private ProgressDialog progressDialog;


    private GridRecyclerFragment gridFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        movies = new ArrayList<>();

        gridFragment = new GridRecyclerFragment();

        setupLazyLoad();

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Fetching...");
        progressDialog.setCancelable(false);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        viewPager = (ViewPager) findViewById(R.id.viewpager);
        setupViewPager(viewPager);

        tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        searchView = (SearchView) MenuItemCompat.getActionView(menu.findItem(R.id.action_search));
        SearchManager searchManager = (SearchManager) getSystemService(SEARCH_SERVICE);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));

        SearchView.OnQueryTextListener queryTextListener = new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                HomeActivity.this.latestQuery = query;
                getData(query, true);
                if (progressDialog != null && !progressDialog.isShowing()) {
                    progressDialog.show();
                }
                searchView.clearFocus();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        };
        searchView.setOnQueryTextListener(queryTextListener);

        return super.onCreateOptionsMenu(menu);
    }

    public void getData(final String query, boolean newQuery) {
        if (newQuery) {
            movies.clear();
            gridFragment.gridRecyclerAdapter.notifyDataSetChanged();
            pagesLoaded = 1;
            reachedEnd = false;
                 ApiCall.Factory.getInstance().searchMovie("a4f7d196", query, String.valueOf(pagesLoaded)).enqueue(new Callback<MovieSearchList>() {
                @Override
                public void onResponse(Call<MovieSearchList> call, Response<MovieSearchList> response) {


                    if(response.isSuccessful()){
                        Log.e("ttttette", "response 1133313: "+response.toString() );
                    }
                    Log.d("movieearchRequestUrl", String.valueOf(call.request()));
                    Log.d("MovieSearchResponsecode", String.valueOf(response.code()));

                   String apiResponse = response.body().toString();
                    Log.d("apiResponse", apiResponse);

                    if (response.body() instanceof MovieSearchList ){
                        MovieSearchList movieSearchList = (MovieSearchList) response.body();
                        if (movieSearchList.getResponse().equals("True")) {
                            //Movie Found
                            pagesLoaded++;
                            getSearchMovies(movieSearchList);
                            gridFragment.message.setVisibility(View.GONE);
                            gridFragment.movieGridRecycler.setVisibility(View.VISIBLE);
                            progressDialog.dismiss();
                        } else {
                            //Movie not found
                            progressDialog.dismiss();
                            gridFragment.message.setText("No movies found. Try again.");
                            gridFragment.message.setVisibility(View.VISIBLE);
                            gridFragment.movieGridRecycler.setVisibility(View.GONE);
                        }
                   }

                }

                @Override
                public void onFailure(Call<MovieSearchList> call, Throwable t) {
                    Log.e(TAG, "Failure : " + t.getMessage());
                    progressDialog.dismiss();
                    gridFragment.message.setText("Query request failed. Try again");
                    gridFragment.message.setVisibility(View.VISIBLE);
                    gridFragment.movieGridRecycler.setVisibility(View.GONE);
                }
            });
        } else {
            if (!reachedEnd) {
                ApiCall.Factory.getInstance().searchMovie("a4f7d196",query, String.valueOf(pagesLoaded)).enqueue(new Callback<MovieSearchList>() {
                    @Override
                    public void onResponse(Call<MovieSearchList> call, Response<MovieSearchList> response) {

                        Log.d("movieearchRequestUrl", String.valueOf(call.request()));
                        Log.d("MovieSearchResponsecode", String.valueOf(response.code()));
                        if (response.body() instanceof MovieSearchList ) {
                            MovieSearchList movieSearchList = (MovieSearchList) response.body();
                            if (movieSearchList.getResponse().equals("True")) {
                                //Movie Found
                                pagesLoaded++;
                                getSearchMovies(movieSearchList);
                            } else {
                                //Reached End
                                movies.remove(movies.size() - 1);
                                reachedEnd = true;
                                gridFragment.gridRecyclerAdapter.notifyItemRemoved(movies.size());
                                gridFragment.gridRecyclerAdapter.notifyDataSetChanged();
                                gridFragment.setLoaded();
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<MovieSearchList> call, Throwable t) {
                        progressDialog.dismiss();
                        movies.remove(movies.size() - 1);
                        gridFragment.gridRecyclerAdapter.notifyItemRemoved(movies.size());
                        gridFragment.gridRecyclerAdapter.notifyDataSetChanged();
                        gridFragment.setLoaded();
                    }
                });
            }
        }
    }




    public void getSearchMovies(MovieSearchList movieSearchList) {
        final int[] count = {0};
        for (int i = 0; i < movieSearchList.getSearch().size(); i++) {
            progressDialog.dismiss();

            movies.add(movieSearchList.getSearch().get(i));
            }

            gridFragment.gridRecyclerAdapter.notifyDataSetChanged();
            gridFragment.setLoaded();

    }





/*    public void getMovies(MovieSearchList movieSearchList) {
        final int[] count = {0};
       for (int i = 0; i < searchResult.getSearch().size(); i++) {
      //      String imdbId =movieSearchList.getSearch().getImdbID();
            ApiCall.Factory.getInstance().getMovie(imdbId).enqueue(new Callback<MovieModel>() {
                @Override
                public void onResponse(Call<MovieModel> call, Response<MovieModel> response) {
                    movies.add(response.body());
                    Log.d(TAG, movies.get(movies.size() - 1).getTitle());
                    count[0]++;
                    isDataFetchComplete(count[0]);
                }

                @Override
                public void onFailure(Call<MovieModel> call, Throwable t) {
                    Log.e(TAG, "Failure : " + t.getMessage());
                    count[0]++;
                    isDataFetchComplete(count[0]);
                }
            });
        }
    }*/

    private void isDataFetchComplete(int count) {
        if (searchResult.getResponse().equals("True") && count == searchResult.getSearch().size()) {
            progressDialog.dismiss();
            for (int i = 0; i < movies.size(); i++) {
                if (movies.get(i) == null) {
                    movies.remove(i);
                    gridFragment.gridRecyclerAdapter.notifyItemRemoved(i);
                }
            }
            gridFragment.gridRecyclerAdapter.notifyDataSetChanged();
            gridFragment.setLoaded();
        }
    }

    private void setupLazyLoad() {
        gridFragment.setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void onLoadMore() {
                if (!reachedEnd) {
                    movies.add(null);
                    gridFragment.gridRecyclerAdapter.notifyItemInserted(movies.size() - 1);
                    getData(latestQuery, false);
                }
            }
        });
    }

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(gridFragment, "GRID");
        viewPager.setAdapter(adapter);
    }

    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }
}

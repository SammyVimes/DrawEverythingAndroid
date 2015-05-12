package ru.edikandco.draweverything.activity;

import android.app.SearchManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.BaseColumns;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.CursorAdapter;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.webkit.DownloadListener;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import httpimage.HttpImageManager;
import ru.edikandco.draweverything.R;
import ru.edikandco.draweverything.core.adapter.BaseAdapter;
import ru.edikandco.draweverything.core.model.Lesson;
import ru.edikandco.draweverything.core.model.LessonSuggestion;
import ru.edikandco.draweverything.core.service.API;
import ru.edikandco.draweverything.core.service.DownloadManager;
import ru.edikandco.draweverything.core.util.Constants;
import ru.edikandco.draweverything.core.util.Promise;
import ru.edikandco.draweverything.core.util.ServiceContainer;
import ru.edikandco.draweverything.core.util.Utils;
import ru.edikandco.draweverything.dialog.MyProgressDialog;

public class LessonsActivity extends BaseToolbarActivity implements AdapterView.OnItemClickListener {

    public static final String CURSOR_ID = BaseColumns._ID;
    public static final String CURSOR_NAME = SearchManager.SUGGEST_COLUMN_TEXT_1;
    public static final String CURSOR_LINK = "LINK";

    public static final String[] COLUMNS = {CURSOR_ID, CURSOR_NAME, CURSOR_LINK};

    private HttpImageManager httpImageManager = null;
    private GridView gridView = null;
    private SearchView searchView;

    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lessons);
        httpImageManager = ServiceContainer.getService(HttpImageManager.class);
        gridView = findViewWithId(R.id.grid_view);
        progressBar = findViewWithId(R.id.progressBar);
        showProgressBar();
        Promise<List<Lesson>> promise = Promise.run(new Promise.PromiseRunnable<List<Lesson>>() {
            @Override
            public void run(final Promise<List<Lesson>>.Resolver resolver) {
                API api = ServiceContainer.getService(API.class);
                try {
                    List<Lesson> lessons = api.getLessons(0);
                    resolver.resolve(lessons);
                } catch (Exception e) {
                    e.printStackTrace();
                    resolver.except(e);
                }
            }
        }, true);
        promise.then(new Promise.Action<List<Lesson>, Void>() {
            @Override
            public Void action(final List<Lesson> data, final boolean success) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        hideProgressBar();
                        showSearchResult(data);
                    }
                });
                return null;
            }
        });
        promise.catchException(new Promise.Action<Exception, Object>() {
            @Override
            public Object action(final Exception data, final boolean success) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Utils.showToast(getApplicationContext(), "Error: " + data.getMessage());
                    }
                });
                return null;
            }
        });
        gridView.setOnItemClickListener(this);

    }

    private void showSearchResult(final List<Lesson> results) {
        gridView.setAdapter(new LessonsAdapter(getApplicationContext(), 0, results));
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.search_menu, menu);
        setupSearchView(menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private DownloadManager downloadManager = null;

    @Override
    public void onItemClick(final AdapterView<?> adapterView, final View view, final int i, final long l) {
        final API api = ServiceContainer.getService(API.class);
        final Lesson lesson = (Lesson) adapterView.getAdapter().getItem(i);
        Promise.run(new Promise.PromiseRunnable<List<String>>() {
            @Override
            public void run(final Promise<List<String>>.Resolver resolver) {
                resolver.resolve(api.getLessonPaths(lesson));
            }
        }, true).then(new Promise.Action<List<String>, Void>() {
            @Override
            public Void action(final List<String> data, final boolean success) {
                File file = new File(Constants.SDPATH + "/" + lesson.getId() + "/");
                file.mkdirs();
                if (downloadManager != null) {
                    downloadManager.kill();
                }
                downloadManager = new DownloadManager();
                final MyProgressDialog myProgressDialog = MyProgressDialog.createDialog(LessonsActivity.this, data.size());

                FragmentManager fragmentManager = getSupportFragmentManager();
                myProgressDialog.show(fragmentManager, "progress");

                downloadManager.setListener(new DownloadManager.DownloadProgressListener() {

                    private int size = data.size();
                    private int cur = 0;

                    @Override
                    public void onProgress(final DownloadManager.Download download, final int progress) {

                    }

                    @Override
                    public void onPause(final DownloadManager.Download download) {

                    }

                    @Override
                    public void onResume(final DownloadManager.Download download) {

                    }

                    @Override
                    public void onComplete(final DownloadManager.Download download) {

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                cur++;
                                myProgressDialog.setProgress(cur);
                                if (cur >= size) {
                                    myProgressDialog.dismiss();
                                    Intent intent = new Intent(LessonsActivity.this, LessonActivity.class);
                                    intent.putExtra("lesson_id", lesson.getId());
                                    intent.putExtra("title_lesson", lesson.getTitle());
                                    startActivity(intent);
                                }
                            }
                        });
                    }

                    @Override
                    public void onCancel(final DownloadManager.Download download) {

                    }

                    @Override
                    public void onError(final DownloadManager.Download download, final String errorMsg) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getApplicationContext(), getString(R.string.error) + errorMsg, Toast.LENGTH_LONG).show();
                            }
                        });
                        downloadManager.kill();
                    }
                });
                int i = 0;
                for (String path : data) {
                    downloadManager.startDownload(path, Constants.SDPATH + "/" + lesson.getId() + "/" + i + ".png");
                    i++;
                }

                return null;
            }
        });
    }

    private class LessonsAdapter extends BaseAdapter<Holder, Lesson> {


        public LessonsAdapter(final Context context, final int resource, final List<Lesson> objects) {
            super(context, resource, objects);
        }

        @Override
        public void onBindViewHolder(final Holder holder, final int position) {
            Lesson lesson = getItem(position);

            holder.title.setText(lesson.getTitle());
            Uri coverUri = Uri.parse(lesson.getCoverURI());
            HttpImageManager.LoadRequest request = HttpImageManager.LoadRequest.obtain(coverUri, holder.cover, 0);
            Bitmap bitmap = httpImageManager.loadImage(request);
            if (bitmap != null) {
                holder.cover.setImageBitmap(bitmap);
            }
        }

        @Override
        public Holder onCreateViewHolder(final ViewGroup viewGroup, final int position) {
            View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.grid_item, viewGroup, false);
            return new Holder(v);
        }

    }

    private void setupSearchView(final Menu menu) {
        searchView = (SearchView) MenuItemCompat.getActionView(menu.findItem(R.id.search));
        searchView.setSubmitButtonEnabled(true);
        searchView.setIconifiedByDefault(false);
        searchView.setFocusable(false);
        searchView.clearFocus();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            private long lastSuggestionUpdateTime = 0;
            private SuggestionsTask suggestionsTask = null;

            private int DELAY = 300;

            @Override
            public boolean onQueryTextSubmit(final String query) {
                QueryTask task = new QueryTask();
                task.execute(query);
                closeKeyboard();
                LessonSuggestionsAdapter adapter = (LessonSuggestionsAdapter) searchView.getSuggestionsAdapter();
                if (adapter == null) {
                    adapter = new LessonSuggestionsAdapter(getApplicationContext(), new MatrixCursor(COLUMNS));
                    searchView.setSuggestionsAdapter(adapter);
                } else {
                    adapter.changeCursor(new MatrixCursor(COLUMNS));
                }
                adapter.setSuggestions(new ArrayList<LessonSuggestion>());
                return true;
            }

            @Override
            public boolean onQueryTextChange(final String query) {
                long curTime = System.currentTimeMillis();
                if (curTime - lastSuggestionUpdateTime < DELAY) {
                    return false;
                }
                lastSuggestionUpdateTime = curTime;
                if (suggestionsTask != null) {
                    suggestionsTask.cancel(true);
                }
                suggestionsTask = new SuggestionsTask();
                suggestionsTask.execute(query);
                return true;
            }

        });
        searchView.setQueryRefinementEnabled(true);
        MatrixCursor matrixCursor = new MatrixCursor(COLUMNS);
        searchView.setSuggestionsAdapter(new LessonSuggestionsAdapter(getApplicationContext(), matrixCursor));
        searchView.setOnSuggestionListener(new SearchView.OnSuggestionListener() {

            @Override
            public boolean onSuggestionSelect(final int i) {
                return false;
            }

            @Override
            public boolean onSuggestionClick(final int i) {
                LessonSuggestionsAdapter adapter = (LessonSuggestionsAdapter) searchView.getSuggestionsAdapter();
                LessonSuggestion suggestion = adapter.getSuggestions().get(i);
//                Less manga = new Manga(suggestion.getTitle(), suggestion.getUrl(), suggestion.getRepository());
//                Intent intent = new Intent(context, MangaInfoActivity.class);
//                intent.putExtra(Constants.MANGA_PARCEL_KEY, manga);
//                startActivity(intent);
                return true;
            }
        });
    }

    private static class Holder extends BaseAdapter.BaseHolder {

        public TextView title;
        public ImageView cover;

        protected Holder(final View view) {
            super(view);
            title = findViewById(R.id.title);
            cover = findViewById(R.id.cover);
        }

    }

    private class QueryTask extends AsyncTask<String, Void, List<Lesson>> {

        @Override
        protected void onPreExecute() {
            showProgressBar();
        }

        @Override
        protected List<Lesson> doInBackground(final String... params) {
            if (params == null || params.length < 1) {
                return null;
            }
            API api = ServiceContainer.getService(API.class);
            try {
                return api.search(params[0], 0);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(final List<Lesson> results) {
            hideProgressBar();
            if (results == null) {
                return;
            }
            showSearchResult(results);
        }

    }

    private class SuggestionsTask extends AsyncTask<String, Void, List<LessonSuggestion>> {

        @Override
        protected void onPreExecute() {

        }

        @Override
        protected List<LessonSuggestion> doInBackground(final String... params) {
            try {
                API api = ServiceContainer.getService(API.class);
                return api.getSuggestions(params[0]);
            } catch (Exception e) {
                //can't load suggestions, nevermind
            }
            return null;
        }

        @Override
        protected void onPostExecute(final List<LessonSuggestion> lessonSuggestions) {
            if (lessonSuggestions == null) {
                return;
            }
            MatrixCursor cursor = new MatrixCursor(COLUMNS);
            int idx = 0;
            for (LessonSuggestion suggestion : lessonSuggestions) {
                String[] row = new String[3];
                row[0] = String.valueOf(idx);
                row[1] = suggestion.getTitle();
                row[2] = suggestion.getLink();
                cursor.addRow(row);
                idx++;
            }
            LessonSuggestionsAdapter adapter = (LessonSuggestionsAdapter) searchView.getSuggestionsAdapter();
            if (adapter == null) {
                adapter = new LessonSuggestionsAdapter(getApplicationContext(), cursor);
                searchView.setSuggestionsAdapter(adapter);
            } else {
                adapter.changeCursor(cursor);
            }
            adapter.setSuggestions(lessonSuggestions);
            searchView.setQueryRefinementEnabled(true);
        }

    }

    private class LessonSuggestionsAdapter extends CursorAdapter {

        private List<LessonSuggestion> suggestions = null;

        public LessonSuggestionsAdapter(final Context context, final Cursor c) {
            super(context, c, 0);
        }

        public List<LessonSuggestion> getSuggestions() {
            return suggestions;
        }

        public void setSuggestions(final List<LessonSuggestion> suggestions) {
            this.suggestions = suggestions;
        }

        @Override
        public View newView(final Context context, final Cursor cursor, final ViewGroup parent) {
            LayoutInflater inflater = LayoutInflater.from(context);
            return inflater.inflate(R.layout.suggestions_list_item, parent, false);
        }

        @Override
        public void bindView(final View view, final Context context, final Cursor cursor) {
            TextView tv = (TextView) view.findViewById(R.id.text1);
            int textIndex = cursor.getColumnIndex(CURSOR_NAME);
            final String value = cursor.getString(textIndex);
            ImageButton btn = (ImageButton) view.findViewById(R.id.suggestion_merge);
            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {
                    searchView.setQuery(value, false);
                }
            });
            tv.setText(value);
        }

    }

    public void showProgressBar() {
        progressBar.setVisibility(View.VISIBLE);
    }

    public void hideProgressBar() {
        progressBar.setVisibility(View.INVISIBLE);
    }

    public void closeKeyboard() {
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(findViewById(android.R.id.content).getWindowToken(), 0);
    }

}

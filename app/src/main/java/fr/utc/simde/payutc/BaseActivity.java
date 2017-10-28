package fr.utc.simde.payutc;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;

import com.fasterxml.jackson.databind.JsonNode;

import fr.utc.simde.payutc.tools.CASConnexion;
import fr.utc.simde.payutc.tools.Dialog;
import fr.utc.simde.payutc.tools.HTTPRequest;
import fr.utc.simde.payutc.tools.NemopaySession;

/**
 * Created by Samy on 26/10/2017.
 */

public abstract class BaseActivity extends NFCActivity {
    private static final String LOG_TAG = "_BaseActivity";
    protected static Dialog dialog;
    protected static NemopaySession nemopaySession;
    protected static CASConnexion casConnexion;

    protected void disconnect() {
        nemopaySession.disconnect();
        casConnexion.disconnect();
    }

    protected void unregister(final Activity activity) {
        nemopaySession.unregister();
        disconnect();

        dialog.errorDialog(activity, getString(R.string.key_registration), getString(R.string.key_remove_temp));
    }

    protected void fatal(final Activity activity, final String title, final String message) {
        dialog.fatalDialog(activity, title, message, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Intent intent = new Intent(activity, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |  Intent.FLAG_ACTIVITY_SINGLE_TOP);
                activity.startActivity(intent);
            }
        });

        disconnect();
    }

    protected void startFoundationListActivity(final Activity activity) {
        dialog.startLoading(activity, getString(R.string.information_collection), getString(R.string.foundation_list_collecting));
        final Intent intent = new Intent(activity, FoundationListActivity.class);

        new Thread() {
            @Override
            public void run() {
                try { // Toute une série de vérifications avant de lancer l'activité
                    if (nemopaySession.getFoundations() != 200)
                        throw new Exception("HTTP Error: " + Integer.toString(nemopaySession.getRequest().getResponseCode()));

                    Thread.sleep(100);
                    final HTTPRequest request = nemopaySession.getRequest();
                    final JsonNode foundationList = request.getJsonResponse();

                    if (!request.isJsonResponse() || !foundationList.isArray())
                        throw new Exception("Malformed JSON");

                    if (foundationList.size() == 0) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                dialog.stopLoading();
                                fatal(activity, getString(R.string.information_collection), nemopaySession.getUsername() + " " + getString(R.string.user_no_rights));
                            }
                        });

                        return;
                    }

                    for (final JsonNode foundation : foundationList) {
                        if (!foundation.has("name") || !foundation.has("fun_id"))
                            throw new Exception("Unexpected JSON");
                    }

                    if (foundationList.size() == 1) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                dialog.stopLoading();
                                startArticlesActivity(activity, foundationList.get(0).get("fun_id").intValue(), foundationList.get(0).get("name").textValue());
                            }
                        });

                        return;
                    }

                    intent.putExtra("foundationList", request.getResponse());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            dialog.stopLoading();
                            activity.startActivity(intent);
                        }
                    });
                } catch (Exception e) {
                    Log.e(LOG_TAG, "error: " + e.getMessage());

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            dialog.errorDialog(activity, getString(R.string.information_collection), getString(R.string.foundation_error_get_list));
                        }
                    });
                }

            }
        }.start();
    }

    protected void startCategoryArticlesActivity(final Activity activity) {
        dialog.startLoading(activity, activity.getResources().getString(R.string.information_collection), activity.getResources().getString(R.string.category_list_collecting));
        final Intent intent = new Intent(activity, ArticleCategoryActivity.class);

        new Thread() {
            @Override
            public void run() {

                try { // Toute une série de vérifications avant de lancer l'activité
                    if (nemopaySession.getCategories() != 200)
                        throw new Exception("HTTP Error: " + Integer.toString(nemopaySession.getRequest().getResponseCode()));

                    Thread.sleep(100);
                    final HTTPRequest request = nemopaySession.getRequest();
                    final JsonNode categoryList = request.getJsonResponse();

                    if (!request.isJsonResponse() || !categoryList.isArray())
                        throw new Exception("Malformed JSON");

                    if (categoryList.size() == 0) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                dialog.stopLoading();

                                dialog.errorDialog(activity, getString(R.string.information_collection), nemopaySession.getFoundationName() + " " + getString(R.string.category_error_0));
                            }
                        });

                        return;
                    }

                    for (final JsonNode category : categoryList) {
                        if (!category.has("id") || !category.has("name") || !category.has("fundation_id") || category.get("fundation_id").intValue() != nemopaySession.getFoundationId())
                            throw new Exception("Unexpected JSON");
                    }

                    intent.putExtra("categoryList", request.getResponse());
                } catch (Exception e) {
                    Log.e(LOG_TAG, "error: " + e.getMessage());

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            dialog.errorDialog(activity, getString(R.string.information_collection), getString(R.string.category_error_get_list));
                        }
                    });

                    return;
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        dialog.changeLoading(getResources().getString(R.string.article_list_collecting));
                    }
                });

                try { // Toute une série de vérifications avant de lancer l'activité
                    if (nemopaySession.getArticles() != 200)
                    throw new Exception("HTTP Error: " + Integer.toString(nemopaySession.getRequest().getResponseCode()));

                    Thread.sleep(100);
                    final HTTPRequest request = nemopaySession.getRequest();
                    final JsonNode articleList = request.getJsonResponse();

                    if (!request.isJsonResponse() || !articleList.isArray())
                        throw new Exception("Malformed JSON");

                    if (articleList.size() == 0) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                dialog.stopLoading();
                                dialog.errorDialog(activity, getString(R.string.information_collection), nemopaySession.getFoundationName() + " " + getString(R.string.article_error_0));
                            }
                        });

                        return;
                    }

                    for (final JsonNode article : articleList) {
                        if (!article.has("id") || !article.has("price") || !article.has("name") || !article.has("active") || !article.has("cotisant") || !article.has("alcool") || !article.has("categorie_id") || !article.has("image_url") || !article.has("fundation_id") || article.get("fundation_id").intValue() != nemopaySession.getFoundationId())
                            throw new Exception("Unexpected JSON");
                    }

                    intent.putExtra("articleList", request.getResponse());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            dialog.stopLoading();
                            activity.startActivity(intent);
                        }
                    });
                } catch (Exception e) {
                    Log.e(LOG_TAG, "error: " + e.getMessage());

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            dialog.errorDialog(activity, getString(R.string.information_collection), getString(R.string.article_error_get_list));
                        }
                    });
                }

            }
        }.start();
    }

    protected void startArticlesActivity(final Activity activity, final int foundationId, final String foundationName) {
        nemopaySession.setFoundation(foundationId, foundationName);
        Log.d(LOG_TAG, String.valueOf(foundationId));

        // Plus tard, on pourra choisir quelle activité lancer
        startCategoryArticlesActivity(activity);
    }
}

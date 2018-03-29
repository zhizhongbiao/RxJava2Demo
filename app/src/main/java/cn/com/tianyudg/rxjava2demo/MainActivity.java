package cn.com.tianyudg.rxjava2demo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import cn.com.tianyudg.rxjava2demo.util.LogUtils;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableSource;
import io.reactivex.Observer;
import io.reactivex.Single;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.BiFunction;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Predicate;
import io.reactivex.schedulers.Schedulers;


public class MainActivity extends AppCompatActivity {

    private TextView textView;
    private ArrayList<List<String>> outList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView = (TextView) findViewById(R.id.tvText);
        outList = new ArrayList<>();

        for (int i = 0; i < 5; i++) {
            ArrayList<String> inList = new ArrayList<>();
            for (int j = 0; j < 10; j++) {
                inList.add("inList - " + j);
            }
            outList.add(inList);
        }
    }


    public void basicTestClick(View view) {
        Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(@NonNull ObservableEmitter<String> emitter) throws Exception {

                emitter.onNext("one");
                emitter.onNext("two");
                emitter.onNext("three");
                emitter.onNext("four");
                emitter.onComplete();

            }
        }).subscribe(new Observer<String>() {

            Disposable mDisposable;

            @Override
            public void onSubscribe(@NonNull Disposable d) {
                mDisposable = d;
            }

            @Override
            public void onNext(@NonNull String s) {
                if ("three".equals(s) && !mDisposable.isDisposed()) {
                    mDisposable.dispose();
                }
                LogUtils.e(" onNext  s = " + s);
            }

            @Override
            public void onError(@NonNull Throwable e) {
                LogUtils.e(" onError    e = " + e.getMessage());
            }

            @Override
            public void onComplete() {
                LogUtils.e(" onComplete");
            }
        });
    }


    public void consumerTestClick(View view) {
        Observable
                .just(1, 2, 23, 4)
                .doOnNext(new Consumer<Integer>() {
                    @Override
                    public void accept(Integer integer) throws Exception {

                        LogUtils.e(" doOnNext integer =" + integer + " - - - Thread = " + Thread.currentThread().getName());
                    }
                })
//                .subscribeOn(Schedulers.newThread())
                .observeOn(Schedulers.io())
                .subscribe(new Consumer<Integer>() {
                    @Override
                    public void accept(Integer integer) throws Exception {
                        LogUtils.e(" subscribe integer =" + integer + " - - - Thread = " + Thread.currentThread().getName());
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        LogUtils.e(" Error   msg :　" + throwable.getMessage());
                    }
                });
    }

    public void concatTestClick(View view) {
        Observable<String> localData = Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(@NonNull ObservableEmitter<String> emitter) throws Exception {
                LogUtils.e(" localData ObservableOnSubscribe - - - Thread = " + Thread.currentThread().getName());

                if (getLocalDataSuccessful()) {
                    emitter.onNext("localData...");
                } else {
                    emitter.onComplete();
                }
            }
        });


        Observable<String> networkData = Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(@NonNull ObservableEmitter<String> emitter) throws Exception {
                emitter.onNext("networkData...");
                LogUtils.e(" networkData - - ObservableOnSubscribe - - - Thread = " + Thread.currentThread().getName());
            }
        });


        Observable
                .concat(localData, networkData)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(String s) throws Exception {
                        LogUtils.e("concat  s = " + s + " - - - Thread = " + Thread.currentThread().getName());
                    }
                });


    }

    private boolean getLocalDataSuccessful() {

        return true;
    }


    public void zipTestClick(View view) {
        Observable<Integer> integer = Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(@NonNull ObservableEmitter<Integer> emitter) throws Exception {
                emitter.onNext(999);
                LogUtils.e(" Integer - - ObservableOnSubscribe - - - Thread = " + Thread.currentThread().getName());
            }
        });

        Observable<String> str = Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(@NonNull ObservableEmitter<String> emitter) throws Exception {
                emitter.onNext("String");
                LogUtils.e(" String - - ObservableOnSubscribe - - - Thread = " + Thread.currentThread().getName());
            }
        });

        Observable
                .zip(integer, str, new BiFunction<Integer, String, String>() {
                    @Override
                    public String apply(@NonNull Integer integer, @NonNull String s) throws Exception {
                        LogUtils.e(" zip - - (" + s + integer + ") - - - Thread = " + Thread.currentThread().getName());
                        return s + " + integer " + integer;
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(String s) throws Exception {
                        LogUtils.e(" subscribe - - Consumer  final result=" + s + " - - - Thread = " + Thread.currentThread().getName());
                    }
                });


    }

    public void intervalTestClick(View view) {
        Observable
                .interval(1, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.io())//不起作用，默认运行在RxComputationThreadPool线程
//                .observeOn(AndroidSchedulers.mainThread())//这个起作用
                .subscribe(new Consumer<Long>() {
                    @Override
                    public void accept(Long aLong) throws Exception {
                        LogUtils.e(" Consumer aLong=" + aLong + "- - - Thread = " + Thread.currentThread().getName());
                    }
                });
    }

    public void filterTestClick(View view) {
        Observable
                .just(1, 8, 33, -54, 67, -4)
                .filter(new Predicate<Integer>() {
                    @Override
                    public boolean test(@NonNull Integer integer) throws Exception {
                        return integer >= 33;
                    }
                })
                .subscribe(new Consumer<Integer>() {
                    @Override
                    public void accept(Integer integer) throws Exception {
                        LogUtils.e("filter integer=" + integer);
                    }
                });
    }

    public void bufferTestClick(View view) {
        Observable.just(1, 3, 4, 5, 7, 78)
                .buffer(2, 3)
                .subscribe(new Consumer<List<Integer>>() {
                    @Override
                    public void accept(List<Integer> integers) throws Exception {
                        LogUtils.e("buffer size=" + integers.size());
                        for (Integer integer : integers) {
                            LogUtils.e(" value = " + integer);
                        }
                    }
                });
    }

    public void timerTestClick(View view) {
        Observable
                .timer(2, TimeUnit.SECONDS)
                .subscribe(new Consumer<Long>() {
                    @Override
                    public void accept(Long aLong) throws Exception {
                        LogUtils.e("long = " + aLong);
                    }
                });
    }

    public void skipTestClick(View view) {
        Observable.just(1, 2, 3, 4, 5, 6, 7)
                .skip(2)
                .subscribe(new Consumer<Integer>() {
                    @Override
                    public void accept(Integer integer) throws Exception {
                        LogUtils.e("integer = " + integer);
                    }
                });
    }

    public void takeTestClick(View view) {
        Observable.just(2, 4, 5, 6, 7, 8, 9)
                .take(3)
                .subscribe(new Consumer<Integer>() {
                    @Override
                    public void accept(Integer integer) throws Exception {
                        LogUtils.e("integer = " + integer);
                    }
                });
    }

    public void singleTestClick(View view) {
        Single
                .just(new Random().nextInt())
                .subscribe(new SingleObserver<Integer>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {

                    }

                    @Override
                    public void onSuccess(@NonNull Integer integer) {
                        LogUtils.e(" integer =" + integer);
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        LogUtils.e(" Throwable =" + e.getMessage());
                    }
                });
    }

    public void distinctTestClick(View view) {
        Observable.just(1, 2, 2, 2, 2, 2, 3, 4, 5, 6)
                .distinct()
                .subscribe(new Consumer<Integer>() {
                    @Override
                    public void accept(Integer integer) throws Exception {
                        LogUtils.e("integer = " + integer);
                    }
                });
    }

    public void debounceTestClick(View view) {
        Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(@NonNull ObservableEmitter<Integer> emitter) throws Exception {
                emitter.onNext(1); // skip
                Thread.sleep(400);
                emitter.onNext(2); // deliver
                Thread.sleep(505);
                emitter.onNext(3); // skip
                Thread.sleep(100);
                emitter.onNext(4); // deliver
                Thread.sleep(605);
                emitter.onNext(5); // deliver
                Thread.sleep(510);
                emitter.onComplete();

            }
        })
                .debounce(500, TimeUnit.MILLISECONDS)
                .subscribe(new Consumer<Integer>() {
                    @Override
                    public void accept(Integer integer) throws Exception {
                        LogUtils.e("integer = " + integer);
                    }
                });
    }

    public void lastTestClick(View view) {
        Observable
                .just(1,2,3,4,5,6)
                .last(3)//如果为空的默认值
                .subscribe(new Consumer<Integer>() {
                    @Override
                    public void accept(Integer integer) throws Exception {
                        LogUtils.e("integer = " + integer);
                    }
                });
    }

    public void mergeTestClick(View view) {
       Observable.merge(Observable.just(1,2,3),Observable.just(4,5,6))
               .subscribe(new Consumer<Integer>() {
                   @Override
                   public void accept(Integer integer) throws Exception {
                       LogUtils.e("integer = " + integer);
                   }
               });
    }

    public void reduceTestClick(View view) {
        Observable.just(2,4,6,8)
                .reduce(new BiFunction<Integer, Integer, Integer>() {
                    @Override
                    public Integer apply(@NonNull Integer integer, @NonNull Integer integer2) throws Exception {
                        return integer*integer2;
                    }
                })
                .subscribe(new Consumer<Integer>() {
                    @Override
                    public void accept(Integer integer) throws Exception {
                        LogUtils.e("integer = " + integer);
                    }
                });
    }

    public void scanTestClick(View view) {

        Observable
                .just(1,3,6,7)
                .scan(new BiFunction<Integer, Integer, Integer>() {
                    @Override
                    public Integer apply(@NonNull Integer integer, @NonNull Integer integer2) throws Exception {
                        return integer*integer2;
                    }
                })
                .subscribe(new Consumer<Integer>() {
                    @Override
                    public void accept(Integer integer) throws Exception {
                        LogUtils.e("integer = " + integer);
                    }
                });

    }

    public void DeferTestClick(View view) {
        Observable<Integer> defer = Observable.defer(new Callable<ObservableSource<Integer>>() {
            @Override
            public ObservableSource<Integer> call() throws Exception {
                return Observable.just(1, 3, 4, 5);
            }
        });

        defer.subscribe(new Consumer<Integer>() {
            @Override
            public void accept(Integer integer) throws Exception {
                LogUtils.e("integer = " + integer);
            }
        });


    }

    public void windowTestClick(View view) {
        Observable
                .interval(2,TimeUnit.SECONDS)
                .take(15)
                .window(4,TimeUnit.SECONDS)
                .subscribe(new Consumer<Observable<Long>>() {
                    @Override
                    public void accept(Observable<Long> longObservable) throws Exception {
                        LogUtils.e("window " );
                        longObservable
                                .subscribe(new Consumer<Long>() {
                                    @Override
                                    public void accept(Long aLong) throws Exception {
                                        LogUtils.e("aLong = " + aLong);
                                    }
                                });
                    }
                });
    }
}

# Gradle でのテスト

## テストの自動化とビルドツール

### 自動テストにおけるビルドツールの要件

- 環境の差異を吸収できること
- 特定の範囲でテストを実行できること
- スローテストを軽減できる仕組みを持っていること

## Gradle によるアプローチ

### 環境差異の吸収

- 他のビルドツールでもできることではあるが、Gradle だと記述がシンプルになる
- 2つアプローチがある
    - 環境ごとにファイルを用意する
    - 1ファイル内でグループ化して切り替える

#### 環境ごとにファイルを用意する

- 環境ごとにプロパティファイルを用意して、ビルド時にコマンドライン引数でプロジェクトプロパティを与えて切り替える
- 環境ごとにビルドスクリプトを用意して、ビルド時にコマンドライン引数でプロジェクトプロパティを与えて切り替える

##### 環境別のプロパティファイルで切り替える

```properties
#environments/dev/dev.properties
app_url=localhost

#environments/production/env.properties
app_url=xxx.xxx.xxx.xxx
```

```gradle
task showURL << {
    def props = new Properties()
    props.load(new FileInputStream("environments/${env}/env.properties"))
    println "App Server: ${props.app_url}"
}
```

```shell-session
$ gradle -Penv=dev showURL
Picked up _JAVA_OPTIONS: -Dfile.encoding=UTF-8
:showURL
App Server: localhost

BUILD SUCCESSFUL

Total time: 0.837 secs

$ gradle -Penv=production showURL
Picked up _JAVA_OPTIONS: -Dfile.encoding=UTF-8
:showURL
App Server: xxx.xxx.xxx.xxx

BUILD SUCCESSFUL

Total time: 0.776 secs
```

##### 環境別のビルドスクリプトを読み込む

```gradle
// environments/dev/env.gradle
ext.app_url = 'localhost' // 拡張プロパティを使う
// environments/production/env.gradle
ext.app_url = 'xxx.xxx.xxx.xxx'

// build.gradle
apply from: "environments/${env}/env.gradle" // apply() で読み込む
task showURL << {
    println "App Server: ${app_url}"
}
```

#### 1ファイル内でグループ化して切り替える

- Groovy の `ConfigSlurper` クラスを使うと、`environments` というブロックの中に環境ごとの設定を記述しておくことができる

```groovy
// env.conf (env.groovy でもよい)
environments {
    dev {
        app_url = 'localhost'
    }
    production {
        app_url = 'xxx.xxx.xxx.xxx'
    }
}
```

```gradle
// build.gradle
task showURL << {
    def url = new File('config/env.conf').toURL()
    def config = new ConfigSlurper("${env}").parse(url)
    println 'App Server: ' + config.app_url
}
```

### 特定の範囲でテストを実行できること

- パターンマッチングで特定して実行する
- テスティングフレームワークの機能を利用して実行する
- ソースセット単位で実行する

#### パターンマッチングで特定して実行する

- Test タスクの設定ブロックの中で `include()`, `exclude()` を使ってテストクラスを指定する
- テストフィルタ（`filter`）を使う
    - `filter` ブロックを使って指定
    - コマンドライン引数で `--tests` を指定する

##### `include()`, `exclude()` でのクラス指定

```gradle
test {
    exclude '**/*IT.class'  // サフィックスに IT が付いたクラスを除外
}
```

インテグレーション用のテストを定義するなら、

```gradle
task integrationTest(type: Test) {
    include '**/*IT.class'  // サフィックスに IT が付いたクラスを含む
}
```

ただしこのままではテストレポートの出力先が `test` と `integrationTest` で同じになってしまい、後から実行されたほうで上書きされてしまう。

これを避けるにはテストレポートの出力先も分ける必要がある。

```gradle
task integrationTest(type: Test) {
    reports.html.destination = file("${reports.html.destination}/integration")
    reports.junitXml.destination = file("${reports.junitXml.destination}/integration")
    
    // unit test も分けたいのであれば、以下も変更する
    reports.html.destination = file("${reports.html.destination}/unit")
    reports.junitXml.destination = file("${reports.junitXml.destination}/unit")
}
```

##### テストフィルタでのフィルタリング

- `filter` ブロックの中でマッチング条件を指定する
    - `includeTestsMatching` などを使う
- クラス、パッケージ、メソッドレベルなどで制御できる
- `exclude()` のように条件を一致したものを除外する仕組みはない

```gradle
test {
    filter {
        includeTestsMatching '*UT'
    }
    reports.html.destination = file(${reports.html.destination/unit")
    reports.junitXml.destination = file(${reports.junitXml.destination/unit")
}

task integrationTest(type: Test) {
    filter {
        includeTestsMatching = '*IT'
    }
    reports.html.destination = file("${reports.html.destination}/integration")
    reports.junitXml.destination = file("${reports.junitXml.destination}/integration")
}
```

#### テスティングフレームワークの機能を利用して実行する

- JUnit のカテゴリ機能を使う
- JUnit のテストスイート機能を使う

##### JUnit のカテゴリ機能を使う

- JUnit 側で `@Category` を使ってカテゴリクラスを指定してグルーピング

```java
@Category(UnitTests.class)
public class ArgumentParserUT {

}
@Category(IntegrationTests.class)
public class CalculateWithParameterIT {

}
```

- Gradle のビルドスクリプトで `useJUnit` ブロックの中で `includeCategories`, 'excludeCategories' プロパティにカテゴリクラスを指定

```gradle
test {
    useJUnit {
        includeCategories 'com.example.suite.categories.UnitTests'
    }
}
task integrationTest(type: Test) {
    useJUnit {
        includeCategories 'com.example.suite.categories.IntegrationTests'
    }
}
```

この方法では、以下の条件に該当したものをテスト実行対象と見なすため、テストケースの重複が起こる。

- 当該クラスまたはスーパークラスが `TestCase/GroovyTestCase` クラスを継承している
- クラスに `@RunWith` アノテーションが付与されている
- メソッドに `@Test` アノテーションが付与されている

このような場合は、例えば `@RunWith` アノテーションの付いたテストスイートを除外するなどすればよい。

```gradle
test {
    exclude '**/*Suite.class'
    useJUnit {
        includeCategories 'com.example.suite.categories.UnitTests'
    }
}

task integrationTest(type: Test) {
    exclude '**/*Suite.class'
    useJUnit {
        includeCategories 'com.example.suite.categories.IntegrationTests'
    }
}
```

##### JUnit のテストスイート機能を使う

- `@Categories` でグルーピングする方法は、事前にテストクラスにグループを示すマーカーを入れておく必要があるが、この方法は個々のテストクラスには全く手を入れずに、テストスイートクラスを追加するだけでよい
- あとから特定の用途で複数のテストクラスを実行させたいような場合に適している

```java
@RunWith(Suite.class)
@SuiteClasses({ ArgumentParserUT.class, SimpleScoreCalculatorImplUT.class, PaymentReceiptUT.class })
public class SmokeTestSuite {}
```

```gradle 
task smokeTest(type: Test) {
    include '**/SmokeTestSuite.class'
    
    reports.html.destination = file("${reports.html.destination}/smoke")
    reports.junitXml.destination = file("${reports.junitXml.destination}/smoke")
}
```

#### ソースセット単位で実行する

- 設定ファイルやテストデータなども含めてまとめて分割することになる
- テストレベルや開発プロジェクトのフェーズなどの区切りで分けると、テストコードやリソースを上位のディレクトリから一元管理することができる
- マルチプロジェクト化してテストごとプロジェクトに切り分ける方法もある
    - 大掛かりにはなるが、参照ライブラリを分離でき、CIツールで実行する場合にも扱いやすい

```gradle
task integrationTest(type: Test) {
    testClassesDir = sourceSets.integrationTest.output.classesDir
    classpath = sourceSets.integrationTest.runtimeClasspath
    
    reports.html.destination = file("${reports.html.destination}/integration")
    reports.junitXml.destination = file("${reports.junitXml.destination}/integration")
}
```

### スローテスト対策

- 遅いテストを分離して実行する
- テストを並列で実行する
- テスト実行プロセスの JavaVM をチューニングする

#### 遅いテストを分離して実行する

- `include()`, `exclude()`
- テストフィルタ
- カテゴリ化テスト

などを利用して遅いテストを分離する。

テストフィルタを一時的に適用したい場合は、コマンドライン引数で指定することもできる。

```shell-session
$ gradle test --tests com.example.*.*Slowly
```

また、逆に遅いテストだけを取り除いて実行したい場合はカテゴリ化テストを使うのがよい。

#### テストを並列で実行する

- `test` タスクのブロックで `maxParallelForks` を設定する
- 注意点
- Gradle でのテスト並列実行は `maxParallelForks` で設定された各プロセスに対して名前順で均等にテストクラスを振り分けているため、並列数によっては1つのプロセスに重いテストが集中してしまうこともあり得る
- 単純にプロセス数を増やしてもコンテキストスイッチによるオーバーヘッドがある

```gradle
test {
    maxParallelForks = 5
}
```

- `forkEvery` というプロパティを設定すると、この値の数だけテストクラスを実行した後に、テストプロセスをリスタートする
- 大量のテストを実行する際にメモリの空きが十分に取れないと頻繁にガベージコレクションが起きてしまうが、この設定はそれを防ぐ効果がある

```gradle
test {
    forkEvery = 3
}
```

#### テスト実行プロセスの JavaVM をチューニングする

- `test` ブロックの設定にて、テストプロセス用のヒープサイズの設定や JVM オプションを設定できる

```gradle
test {
    minHeapSize = '1g'
    maxHeapSize = '1g'
    jvmArgs = '-XX:UseG1GC'
}
```

- これらの JVM オプションの設定はすべてのテストプロセスが参照するので、並列実行する際にメモリを拡張してしまうとリソースが枯渇してしまう可能性があるので、ビルドスクリプトに記述するのは全体に対する JVM オプション設定だけに留めるべき
- リソースを多く必要とするテストが特定されているようであれば、専用のテストタスクを定義して対象クラスに適した `JavaVM` オプションを設定して実行させるように工夫する

```gradle
task testForHeapEater(type: Test) {
    filter {
        includeTestsMatching '*UsingHugeHeapTest'
    }
    minHeapSize = '1g'
    maxHeapSize = '1g'
}
```

## Gradle での自動テスト

単一のクラスをテストするユニットテスト、複数のクラスを統合してテストするインテグレーションテスト、エンドツーエンドで全体の機能をテストするファンクショナルテストを記述した[サンプルプロジェクト](https://github.com/yukung/yokohamagroovy/tree/master/practice-testing-gradle)を作成したので、そちらの構成を参照のこと。

## テストに関するその他の機能

### ログ出力の制御

- 通常 Gradle でログ出力を制御したい場合は、起動オプションでログレベルを変更するが、この設定はビルドフェーズ全体に反映される
- 例えばテスト実行時だけ標準出力やスタックトレースを出力させたいという場面もある
- テスト対象クラスに実装されたログ出力をコンソールに出力したい場合は次のようにする

```gradle
test {
    testLogging {
        showStandardStreams = true
    }
}
```

- これを応用してログにタイムスタンプを出力させれば、簡易的な性能ログをレポートに書き出せる
- 他にも、テスト実行時のイベントログを詳細に出力する設定もある

```gradle
test {
    testLogging {
        events 'started', 'skipped', 'failed'
    }
}
```

- 上記のようにすると、`FAILED` だけでなく `STARTED` や `SKIPPED` が出力される
- さらに `displayGranularity` プロパティをの値を変更すると、実行対象のテストクラス名とテストメソッド名の粒度を変更することもできる 
- 上手く設定すればイベントログが見やすくなる

### テストレポート出力の制御

- テストレポートは通常テストタスクの単位で出力することが多い
- 個別のテストレポートの他に、集約されたサマリーのレポートが必要な場合もある
- 集約されたサマリーだけあれば良いという場合も
- テストレポートをマージしたり、出力を止めたりすることもできる
- テストレポートをマージする場合は、`TestReport` 型でタスクを定義する

```gradle
sourceSets {
    sub {
        java.srcDir file('src/sub/java')
        compileClasspath = configurations.compile
    }
    subTest {
        java.srcDir file('src/subTest/java')
        compileClasspath = sourceSets.sub.output + configurations.testCompile
        runtimeClasspath = output+ compileClasspath + configuraitons.testRuntime
    }
}

test {
    description = 'src/main/javaに対するテストを実行します'
    
    reports.html.destination = file("${reports.html.destination}/unit-main")
    reports.junitXml.destination = file("${reports.junitXml.destination}/unit-main")
}

task subTest(type: Test) {
    description = 'src/sub/javaに対するテストを実行します'
    group = 'verification'
    
    testClassDir = sourceSets.subTest.output.classesDir
    classpath = sourceSets.subTest.runtimeClasspath
    
    reports.html.destination = file("${reports.html.destination}/unit-sub")
    reports.junitXml.destination = file("${reports.junitXml.destination}/unit-sub")
}
```

- テスト結果をマージしたプロジェクト全体のサマリーは次のような定義を行う

```gradle
task summaryTestReports(type: TestReport, dependsOn: [test, subTest]) {
    destinationDir = file("${buildDir}/reports/tests/unit-summary")
    reportOn test, subTest
}

check.dependsOn summaryTestReports
```

- 個別のテストレポートは省略し、サマリーだけ出力する例

```gradle
test {
    reports.html.enabled = false
}

task subTest(type: Test) {
    reports.html.enabled = false
}
```

### デバッグモードでのテスト

- Gradle ではテスト実行プロセスをデバッグモードで実行することができる
- 仕組みとしては、JavaVM のデバッグモードを Gradle で簡潔に指定出来るだけのこと
- 通常の Java でデバッグ起動する場合は、`-Xdebug`, `-Xrunjdwp` の Java VM オプションを指定する必要がある

```console
-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=<接続ポート>
```

- Gradle のデバッグモードでは2通りの指定方法があるが、どちらも簡単
- 1つ目は`debug` プロパティを有効にするだけ

```gradle
test {
    debug true
}
```

- 2つ目は実行時に `--debug-jvm` をタスクオプションとして追加する
- ただし、Gradle のデバッグモードでは接続ポートが `5005` 固定になるので、変更する場合は通常の JavaVM オプションで指定する必要がある

```console
$ gradle test --debug-jvm
```

- この状態で、任意の IDE 上でブレークポイントを設定してリモートデバッグ接続を開始すれば、IDE 上でデバッグができる
- `--debug-jvm` オプションは、Gradle 1.12 から追加された機能だが、それ以前は `-D<テストタスク>.debug` というコマンド引数を指定すると同様のデバッグが出来た。しかしこれは `Test` タスクにのみ有効であり、`--debug-jvm` オプションは `Test` タスクに加えて `JavaExec` タスクにも有効なので、上位互換と考えて良い。そのため、Gradle 1.12 以上では `--debug-jvm` オプションを使えば良い。
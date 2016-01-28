# エキスパートへの道

## ビルドスクリプトの分割と最適化

### メソッド、クラスの抽出

ビルドスクリプトは Groovy スクリプトなので、メソッドやクラスに抽出することができる。

```gradle
task run << {
    final greeting = new Greeting(content: "Hello")
    printGreeting(greeting)
}

void printGreeting(Greeting greeting) {
    println "Greeting: $greeting.content"
}

class Greeting {
    String content
}
```

## プロジェクトの分割

マルチプロジェクトでは、子プロジェクトが親プロジェクトのプロパティやメソッドを継承する他、インジェクションすることもできる。

```gradle
// build.gradle 親プロジェクト

ext {
  prop1 = "root prop" // 親プロジェクトのプロパティ
}
project(":child1") { // 子プロジェクトchild1へのインジェクション
  ext {
    prop3 = "injected child prop" // プロパティのインジェクション
  }
  task injectedTask << { // タスクのインジェクション
    println "from a injected task"
    // 子プロジェクトchild1のプロパティ, 親プロジェクトから継承されたメソッドの呼び出し
    printProp(prop2)
  }
}

task run << {
  println "from a root task"
  printProp(prop1)
}

void printProp(String prop) {
  println "Prop: $prop"
}

// child1/build.gradle 子プロジェクト
ext {
  prop2 = "child prop1"
}

// 親プロジェクトからインジェクションされたタスクを参照して依存
task run(dependsOn: "injectedTask") << {
  println "from a child task"
  // 親プロジェクトから継承されたメソッド、プロパティの呼び出し
  printProp(prop1)
  // プロジェクトからインジェクションされたプロパティの呼び出し
  printProp(prop3)
}
```

```shell-session
$ gradle run
:run
from a root task
Prop: root prop
:child1:injectedTask
from a injected task
Prop: child prop1
:child1:run
from a child task
Prop: root prop
Prop: injected child prop

BUILD SUCCESSFUL

Total time: 0.96 secs
```

継承を多用するとメソッドやプロパティがどのスクリプトで導入されているのかがわかりにくくなるので、基本的にはインジェクションを使う。

### 外部ライブラリの読み込み

Gradle は Java VM で動くビルドツールなので、クラスパスに JAR ファイルを設定するとビルドスクリプトからそのライブラリを呼び出せる。

`buildscript` ブロックにライブラリを指定すると、クラスパスに設定される。

```gradle
// build.gradle
buildscript {
  repositories {
    mavenCentral()
  }
  dependencies {
    classpath "org.apache.commons:commons-lang3:3.3.1"
  }
}

import org.apache.commons.lang3.StringUtils

task run << {
  println StringUtils.removeEnd("http://www.gradle.org/", "/")
}
```

`buildscript` ブロックを利用すれば、ビルドスクリプトが大きくなってきた時に、ビルドロジックを Java や Groovy クラスに切り出して JAR ファイルに格納し、ビルドスクリプトでその JAR ファイルを参照するように設定できる。

次の例では、サーバーにデプロイする手順をカスタムタスク化し、JAR に切り出して呼び出している。

```gradle
// build.gradle
buildscript {
  repositories {
    flatDir dir: "lib" // lib/mycompany-gradle-tasks-1.0.jarにライブラリを配置
  }
  dependencies {
    classpath "com.example:mycompany-gradle-tasks:1.0"
  }
}

apply plugin: "war"

import com.example.mycompany.gradle.tasks.Deploy

task deploy(type: Deploy) {
  description = "カスタムタスクでWarをデプロイします"

  url "http://mycompany-server.example.com"
}
```

### 外部ビルドスクリプトの読み込み

外部で見つけたビルドスクリプトを自分のビルドスクリプトに取り込む時、そのままコピペしてもいいが、Gradle では外部のビルドスクリプトを直接自分のビルドスクリプトに取り込むことができる。例えば、

    https://raw.githubusercontent.com/xxxuser/sample-buildscript/master/build.gradle

に次のようなビルドスクリプトがあり、それを自分のビルドスクリプトに取り込みたい場合、

```gradle
task externalTask << {
    println "from an external task"
}
```

次のように `apply from` を自分のビルドスクリプトに記述することで、外部のビルドスクリプトを取り込むことができる。

```gradle
apply from: "https://gist.githubusercontent.com/literalice/639c845fe7ea4b7b6ce0/raw/1c254a53460a0afa3fd64b98662d8eb74a93cf2d/gistfile1.groovy"

task run(dependsOn: "externalTask") << {
  println "task execution is complete."
}
```

```shell-session
$ gradle run
:externalTask
from an external task
:run
task execution is complete.

BUILD SUCCESSFUL

Total time: 2.255 secs
```

あたかも `apply from: ` の部分に参照したスクリプトがペーストされたかのように動作する。

この方法で取り込まれるスクリプトを「スクリプト形式のプラグイン」と呼ぶ人もいる。一時的な措置であればこのような方法でプラグインとして機能を切り分けたり、外部からとり込んだりするのも良いが、実際にビルドシステムを構築する際にはクラス形式のカスタムプラグインを使用する方が良い。テストのしやすさ、コードの読みやすさ、配布方法の柔軟性などの観点からクラス形式のプラグインのほうが使いやすい。

`apply from` は、URL だけでなくローカルのファイルパスを指定することもできる。これは環境ごとに設定を切り替える場合などに便利。

### buildSrc プロジェクト

プロジェクトディレクトリに `buildSrc` という名前のディレクトリを作成し、そこに Java や Groovy のコードを配置することで、ビルド時に自動的にコンパイルされ、ビルドスクリプトから参照できるようになる。例として以下の様なディレクトリ構造の Gradle プロジェクトがある。

```
<EXAMPLE_PROJECT>
├── build.gradle
└── buildSrc
    └── src
        └── main
            └── groovy
                └── Greeting.groovy
```

このプロジェクトのビルドスクリプトは次のようになっている。

```gradle
// build.gradle
task run << {
  final greeting = new Greeting(content: "Hello")
  printGreeting(greeting)
}

void printGreeting(Greeting greeting) {
  println "Greeting: $greeting.content"
}
```

さらに、`buildSrc` には `Greeting.groovy` が格納されている。

```groovy
// buildSrc/src/main/groovy/Greeting.groovy
class Greeting {
  String content
}
```

`buildSrc` ディレクトリには上記のファイル以外何もないように見えるが、`buildSrc` ディレクトリは Gradle の Groovy プロジェクトとして設定される。つまり、暗黙的に次のビルドスクリプトが格納されているものとして取り扱われる。

```gradle
apply plugin: "groovy"
dependencies {
    compile gradleApi()
    compile localGroovy()
}
```

また、`buildSrc` ディレクトリにビルドスクリプト（`build.gradle`）を明示的に配置することで、上記のデフォルト設定をカスタマイズすることもできる。その場合でも、上記の暗黙的なビルドスクリプトはマージされるので、同じ内容を再度記述する必要はない。`buildSrc` のビルドスクリプトには暗黙的なスクリプトとの差分だけを記述する。

`buildSrc` プロジェクトは、Java や Groovy のクラスを格納しておくだけでビルドスクリプトから参照できるようになるので、大きくなってきたビルドスクリプト内のロジックを手軽に切り出して放り込めるという意味ではとても便利。

しかし、`buildSrc` プロジェクトがあまり大きくなりすぎると `gradle` コマンドを実行するたびに走るコンパイル処理、テスト処理が煩わしくなってくる。その時は `buildSrc` プロジェクトを独立した Gradle プロジェクトに移し、JAR ファイルをビルドしてから外部ライブラリの読み込みで紹介したような `buildscript` ブロックでその JAR ファイルを参照するようにする。

### カスタムタスクの作成

独自のタスクを定義する方法はこれまでも見てきたとおり、以下のように定義する。

```gradle
task myTask << {
    println "Hello, world!"
}
```

このタスクは定義しているビルドスクリプトを `apply from` で取り込めば、他のビルドスクリプトから再利用できる。しかしこのような方法でタスクを共有すると保守性、可読性、テスタビリティなどを損なうことが多く、基本的にはお勧めしない。以下の様な問題点が挙げられる。

* タスク名が固定される
* カスタムタスクの配布方法が制限される
* ビルドスクリプトのスコープを共有するためタスク名が競合する／不用意にプロジェクトのプロパティを書きかえるなどの事故が起こりやすい
* 後述の `DefaultTask` を継承するタイプのカスタムタスクと違い、テスト方法が整備されていない

Gradle では、独自タスクを定義する方法がもう一つ用意されている。実際には `Task` オブジェクトを生成し、プロジェクトに設定する方法がある。どのクラスの `Task` オブジェクトを作成するかは `type` 属性で指定できる。

```gradle
task myTask(type: DefaultTask) << {
    println "Hello, world!"
}
```

`type` 属性を省略すると `DefaultTask` クラスが使用されるため、上記の `myTask` とこの定義は同じ意味を持ったコード。

`DefaultTask` クラスは動作を何も定義していない。実際にビルドで何か意味のあることをさせるには、`DefaultTask` のオブジェクトを生成した後、そのオブジェクトに実行したい動作を追加する必要がある。

`DefaultTask` クラスを拡張し、デフォルトの動作を追加しておけば、このように実行したい動作を追加する必要はない。また、クラスのプロパティやメソッドを公開することで動作の設定を変更させることも可能。

```gradle
task myTask(type: MyTask) { // 評価フェーズ（Task オブジェクト作成時）に実行
    target = "world"
}

class MyTask extends DefaultTask {
    String target
    MyTask() {
        this << {
            println "Hello, $target!"   // 実行フェーズに実行
        }
    }
}
```

この例では、動作の追加を独自に定義したタスククラスの中で行っている。そのため、タスク宣言時に動作を追加する必要が無い。また、宣言時にタスククラスが公開しているプロパティに値を設定することで、動作のカスタマイズもできるようになっている。また、`DefaultTask` クラスはビルドスクリプトにデフォルトでインポートされるので `import` で指定しなくても使える。

このタスククラスは、ビルドスクリプトから見えるところであればどこにでも置くことができる。つまり、同じスクリプトの中に記述したり、JAR ファイルにして `buildscript` ブロックの中で読み込んだり、`buildSrc` ディレクトリにソースファイルを置いておくなど、様々な方法で共有できる。

また、Gradle は `@TaskAction` アノテーションを提供しており、前述のコードと同じことを次のように書くことができる。

```gradle
task myTask(type: MyTask) { // スクリプト評価フェーズ（Taskオブジェクト作成時）に実行
  target = "world"
}

class MyTask extends DefaultTask {
  String target

  @TaskAction
  void greet() {
    println "Hello, $target!" // スクリプト実行フェーズに実行
  }
}
```

この書き方の方が実行されるアクションが明示的で可読性も高くテストもしやすいので、実際にタスククラスを定義する際は `@TaskAction` を使用することを勧める。

#### タスククラスのテスト

タスククラスは一般的な Java のクラスなので、定義されたタスクに比べてテストは比較的書きやすくなっている。

ただし、`DefaultTask` を継承したタスククラスは通常の方法でインスタンスを作成できない。

```groovy
import org.junit.Test
import static org.junit.Assert.assertEquals
import groovy.ui.SystemOutputInterceptor
import org.gradle.testfixtures.ProjectBuilder

class MyTaskTests {
  @Test
  void helloWorldTest() {
    final project = ProjectBuilder.builder().build() // Gradleプロジェクトの作成
    final task = project.task("myTask", type: MyTask) // テスト対象タスクの作成
    task.target = "world" // タスクプロパティの設定

    final actual = getStdOut {
      task.greet() // テスト対象アクションの実行
    }
    assertEquals "Hello, ${task.target}!".toString(), actual
  }

  private static String getStdOut(Closure proc) {
    def stdOut = ""
    final interceptor = new SystemOutputInterceptor({ stdOut += it; false })
    interceptor.start()
    proc()
    interceptor.stop()
    stdOut.trim()
  }
}
```

このようにタスククラスのテストを書く際は、まず Gradle が提供するテスト用ユーティリティの `ProjectBuilder` クラスを使用して `Project` インスタンスを作成する必要がある。その後、`Project` インスタンスから `task()` を使用してタスククラスのインスタンスを作成するという流れ。`task()` の引数はビルドスクリプトでタスクを定義するときと同じく、タスク名や `type` 。

カスタムクラスの作成については、ここで取り上げたものの他にも、

* スタンドアロンの Gradle プロジェクトを作り、タスククラスをパッケージした JAR ファイルを作成する
* タスクの入出力ファイルを指定してインクリメンタルビルドを有効にする

など、様々なトピックがある。

#### タスククラスのテストコードを配置する場所

`buildSrc` は前述のとおり Gradle の Groovy プロジェクトなので、テスト用のソースディレクトリはデフォルトでは `src/test/groovy` または `src/test/java` である。

先程の例のようなタスクのテストクラスは、`buildSrc` ディレクトリにタスククラスを配置した場合、同じく `buildSrc` のテスト用ソースディレクトリにテストクラスを配置することでビルド実行時に Gradle が自動でテストしてくれる。

一方で `build.gradle` にタスククラスを直接定義した場合、一般的な方法でテストすることは難しいので、ごく簡単なタスククラス以外は基本的に `buildSrc` に置くか、独立したプロジェクトで JAR ファイルにパッケージするのが良い。

### カスタムプラグインの作成


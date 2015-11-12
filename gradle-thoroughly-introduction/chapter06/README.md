# スクリプトファイルの記述

## スクリプトファイルの構造と共通要素

### スクリプトファイルの構造

- ステートメント
- スクリプトブロック

の2つからなる。スクリプトに記述した内容が Gradle のドメインオブジェクトに委譲されて実行される。スクリプトファイルはただの Groovy スクリプトなので、クラスを定義して実行することもできる。上記2つの要素が必ず含まれていなくてもビルドは実行される。また、スクリプトブロックの記述順にも制約はない。

### スクリプトファイルの共通要素

Gradle のスクリプトファイルで扱える変数には、以下の4つがある。

| 名称 | 概要 | 使用可能なスクリプトファイル |
| ---- | ---- | ---------------------- |
| ローカル変数 | 宣言されたスコープで有効な変数 | 全てのスクリプトファイル |
| システムプロパティ | システムの情報を保持するためのプロパティ | 全てのスクリプトファイル |
| 拡張プロパティ | ドメインオブジェクトを拡張するためのプロパティ | 全てのスクリプトファイル |
| プロジェクトプロパティ | プロジェクトで使用するためのプロパティ | ビルドスクリプト |

#### ローカル変数

- 宣言された場所で使用可能なスコープが決まる
    - トップレベルならスクリプト全体
    - スクリプトブロックやタスク内で宣言されたなら括弧の中
- Groovy スクリプトとしては型宣言は本来不要だが、 Gradle スクリプトでは型宣言が必須（`def` キーワードか型を指定）で、指定しないとエラーになる
    - これは、型宣言のないものは変数ではなく Gradle のドメインオブジェクトのプロパティとしてみなされるため
    - 本来、Gradle 1.x 系でダイナミックプロパティという仕組みで動的に Gradle のドメインオブジェクトのプロパティを追加できたが、それが Gradle 2.0 から廃止されたので、同じことしたい場合は拡張プロパティ `ext` を使う

#### システムプロパティ

- コマンドライン引数で指定する場合は `-D (--system-prop)` を使って `<プロパティ名>=<値>` で指定

```gradle
task greet << {
    println System.properties['message']
}
```

```shell-session
$ gradle -Dmessage=Hello greet
:greet
Hello

BUILD SUCCESSFUL
```

- `gradle.properties` に `systemProp` というプレフィックスを付けてプロパティを指定

```properties
# gradle.properties
systemProp.message=Hello
```

```shell-session
$ gradle greet
:greet
Hello

BUILD SUCCESSFUL
```

#### 拡張プロパティ

Gradle のドメインオブジェクトのプロパティを追加する仕組み。`ext` という名前で暗黙的に定義されており、使用するのに特に制約もなく自由に使える。対応するドメインオブジェクトは `ExtraPropertiesExtension`。

```gradle
ext {
    key1 = 'value1'
    key2 = 'value2'
}
ext.key3 = 'value3'
```

#### プロジェクトプロパティ

ビルドスクリプトでのみ利用可能（`Project` オブジェクトに委譲されるため）で、ビルド対象のプロジェクトのプロパティとして使う。内部的には拡張プロパティと同様。違うのは設定方法。

- プロパティファイル
- 環境変数
- コマンドライン引数

のどれでも設定できる。

##### プロパティファイル（`gradle.properties`)

```properties
// gradle.properties
message=Hello
```

```gradle
task greet << {
    println message
}
```

```shell-session
$ gradle greet
:greet
Hello

BUILD SUCCESSFUL
```

##### 環境変数

- `ORG_GRADLE_PROJECT_<プロパティ名>=<値>` という形式で設定できる

```shell-session
$ export ORG_GRADLE_PROJECT_message=Hello
$ gradle greet
:greet
Hello

BUILD SUCCESSFUL
```

##### コマンドライン引数

以下の2通りある。

- プロジェクトプロパティ専用のオプション `-P (--project-prop)` を使う
- システムプロパティの `-D (--system-prop)` を使う

###### `-P (--project-prop)`

```shell-session
$ gradle -Pmessage=Hello greet
:greet
Hello

BUILD SUCCESSFUL
```

###### `-D (--system-prop)`

- `org.gradle.project.` をプレフィックスとして付けることで、プロジェクトプロパティとして識別される

```shell-session
$ gradle -Dorg.gradle.project.message=Hello greet
:greet
Hello

BUILD SUCCESSFUL
```

##### プロジェクトプロパティのロード順

1. プロジェクトディレクトリ直下の `gradle.properties`
2. ユーザーホームディレクトリ直下の `gradle.properties`
3. 環境変数 `ORG_GRADLE_PROJECT_`
4. コマンドライン引数 `-D (--system-prop)`
5. コマンドライン引数 `-P (--project-prop)`

### スクリプトブロックとドメインオブジェクト

スクリプトブロックは、Gradle のドメインオブジェクトのメソッドに対してクロージャとして設定内容を渡すことで実現している。例えば `repositories {}` ブロックでは、

```gradle
repositories {
    mavenCentral()
}
```

と記述するが、これは Groovy のプログラム風に記述すると、

```groovy
def closure = { mavenCentral() }
this.repositories(closure) // この this は Gradle の Project オブジェクト
```

と等価。

また、渡されるクロージャは必ず何かのオブジェクトに委譲されて実行されるので、委譲先のオブジェクトが許容できる内容でないとエラーとなる。委譲先のオブジェクトもまた Gradle のドメインオブジェクトとなる。

`mavenCentral()` の例で言えば、`RepositoryHandler` という Gradle のドメインオブジェクトの `mavenCentral()` メソッドが呼ばれることになる。つまり、以下の記述と等価。

```groovy
RepositoryHandler repos = this.Repositories() // this は Project オブジェクト
repos.mavenCentral()
```

つまり、スクリプトファイルに記述する定義は、Gradle ドメインオブジェクトそのものへの API 呼び出しそのものであることが理解できる。なので、記述内容について調べたい場合は DSL リファレンスと API リファレンスを見れば分かることになる。

### 主要なスクリプトブロック

| スクリプトブロック | 概要 | ドメインオブジェクト |
| --------------- | ---- | ----------------- |
| `initscript` | 初期化スクリプトのクラスパスの設定を記述する | `ScriptHandler` |
| `buildscript` | ビルドスクリプトのクラスパスの設定を記述する | `ScriptHandler` |
| `allprojects` | 当該プロジェクトと全てのサブプロジェクトに関する設定を記述する | `Project` |
| `subprojects` | 当該プロジェクトのサブプロジェクトに関する設定を記述する | `Project` |
| `configurations` | コンフィグレーションの設定を記述する | `ConfigurationContainer` |
| `dependencies` | 依存関係の解決のための設定を記述する | `DependencyHandler` |
| `repositories` | リポジトリの設定を記述する | `RepositoryHandler` |
| `artifacts` | ビルド成果物（アーティファクト）を公開するための設定を記述する | `ArtifactHandler` |

## Gradle ドメインオブジェクト

ここでは以下のドメインオブジェクトについて記述する。

- `Project`
- `Task`
- `Gradle`
- `Settings`
- `ExtensionAware`
- `ExtraPropertiesExtension`

### Project オブジェクト

`Project` オブジェクトはビルドスクリプトから委譲されるオブジェクトで、他のドメインを統括する中心的なオブジェクト。他のドメインオブジェクトを直接または間接的に参照できる。

#### `Project` オブジェクトの構造

- `rootProject` や `parent` といった上位のプロジェクトの `Project` オブジェクトの参照を持ち、マルチプロジェクト構成に対応できるようになっている
- 他のドメインオブジェクトのコンテナ（`TaskContainer`, `DependencyHandler`, 'ArtifactHandler', 'ConfigurationContainer'）などを保持して、それらにビルド情報を移譲する。

#### プロパティ（一部）

- 基本属性
    - `name`
    - `description`
    - `group`
    - `path`
    - `projectDir`
    - `status`
    - `state`
    - `version`
- プロジェクト参照属性
    - `project`
    - `rootProject`
    - `parent`
    - `childProjects`
        - ひとつ下のプロジェクトのみ（孫は持たない）
    - `allprojects`
        - 自分を含む
    - `subprojects`
        - 自分を含まない
- コンテナタイプ属性
    - `repositories`
    - `tasks`
        - これらの多くはスクリプトブロックが用意されていて、そこで設定を行う
        - コンテナタイプのドメインオブジェクトが用意されていれば、スクリプトブロックが使えると思ってよい
    - `gradle`
        - Gradle ドメインオブジェクトへの参照
    - `ant`
        - `AntBuilder` への参照
    - `defaultTasks`
        - デフォルトタスクを `List` 形式で指定
        - 指定した順に実行される
    - etc...

#### 主要な API

##### プロジェクト参照 API

- 多くはプロパティに対応するもの
- マルチプロジェクトで他のプロジェクトの設定をしたい場合など
- `project()` メソッドを使う

```groovy
def child = project(':child')
child.description = '子プロジェクトの説明'
```

これをパスとクロージャを引数とするオーバーロードされたメソッドを使うと、

```gradle
project(':child') {
    description = '子プロジェクトの説明'
}
```

と記述できる。

##### タスク定義 API

- タスクを定義したりタスクにアクセスするためのもの
- `task()` メソッドを使う。`task()` はオーバーロードされていて、よく使うのはタスク名を引数にするもの

```gradle
task hello << {
    println 'Hello Gradle world!'
}
```

これはタスクの定義と処理の追加を同時に行っている。これを API を駆使して書くと

```groovy
def hello = task('hello')
hello << { println 'Hello Gradle world!') }
```

と書ける。

##### コールバック API

Gradle がビルド処理を実行している最中にビルドフェーズ内からコールバックされる API として、

- `beforeEvaluate()`
- `afterEvaluate()`

の2つがある。

`beforeEvaluate()` はプロジェクトの評価前にコールバックされるので、ビルドスクリプトの内容を読み込む前に実行する。そのため、ビルドスクリプトに記述した処理を `beforeEvaluate()` から参照できない。なので、プロジェクトの評価前に独自の処理を割りこませたい場合は、初期化スクリプトまたは設定スクリプトに記述しておく必要がある。

```gradle
// settings.gradle
gradle.allprojects { project ->
    project.beforeEvaluate {
        println project.name + 'プロジェクトを評価します。'
    }
    project.afterEvaluate {
        println project.name + 'プロジェクトを評価しました。'
    }
}
```

##### その他の API

- `javaexec()`
    - Java のメインクラスを実行する
- `exec()`
    - 外部の OS コマンドを実行する
- `tarTree()`
    - 指定された TAR ファイルを基にして `FileTree` を生成する
- `zipTree()`
    - 指定された ZIP ファイルを基にして `FileTree` を生成する

### Task オブジェクト

- 対象作業を表す Gradle のドメインオブジェクト
- ビルドスクリプトで記述されたタスクの定義が `Project` オブジェクトを介して `Task` オブジェクトに委譲されるため、ビルドスクリプトのタスクに関する記述は `Task` オブジェクトへのプロパティの設定や API の呼び出しになる

#### プロパティ（一部）

- 基本属性
    - `name`
    - `description`
    - `group`
        - タスクのグループ。`tasks` タスクを実行した時のグルーピング単位
    - `path`
        - タスクのパス。プロジェクトも含めたパスは `:` がセパレータになる
- タスク実行に関するプロパティ
    - `enabled`
    - `state`
    - `inputs`
    - `outputs`
    - `dependsOn`
    - `mustRunAfter`
    - `shouldRunAfter`
    - `finalizedBy`

タスクの実行順を制御したり、状態によって実行をスキップしたりできる。また、直接触ることはないが、`actions` というプロパティがあり、これはタスクを構成する最小の単位である `Action` オブジェクトのリストであり、これを順次実行することでタスクの実行を実現している。

#### 主要な API

- タスクの処理のためのもの
- タスクの実行に関連するもの
- プロパティのアクセサ

の3種類で占められている。そのうち、タスクの処理を記述するためのものは、

- `doFirst()`
- `doLast()`
- `leftShift()`
- `deleteAllActions()`

等がある。

```gradle
task myActionTask {
    doFirst {
        println 'First!'
    }
}
```

```shell-session
$ gradle myActionTask
:myActionTask
First!
```

```gradle
myActionTask.doLast {
    println 'Last!'
}
```

```shell-session
$ gradle myActionTask
:myActionTask
First!
Last!
```

この挙動は、内部的には次のような処理イメージになっている。

```groovy
List<Action> actions = new ArrayList<>()
// doFirst 相当の処理
Closure doFirstAction = { println 'First!' }
actions.add(0, doFirstAction)

// doLast 相当の処理
Closure doLastAction = { println 'Last!' }
actions.add(doLastAction)

// myActionTask の実行
actions.each { action ->
    action.execute() // println 文を実行
}
```

また、`leftShift()` は、`<<` の演算子オーバーロードで、`doLast()` のショートカット。

```gradle
myActionTask << { println 'Last by leftShift!' }
```

`deleteAllActions()` はタスク（`Action` オブジェクト）を削除する。

```gradle
myActionTask.deleteAllActions()
```

### Gradle オブジェクト

- 実行環境を表す Gradle のドメインオブジェクト
- 初期化スクリプトから委譲される
- `Project` や `Setteings` オブジェクトがプロパティとして保持している
- 設定スクリプトやビルドスクリプトからはプロパティのアクセスが可能なので、 `gradle` を指定して API やプロパティを呼ぶのが一般的。

```gradle
println gradle.gradleVersion
```

#### プロパティ（一部）

- `gradleHomeDir`
- `gradleUserHomeDir`
- `gradleVersion`
- `rootProject`
- `startParameter`
- `taskGraph`

#### 主要なAPI

##### イベントリスナーを追加する API

- `addBuildListener()`
- `addProjectEvaluationListener()`
- `addListener()`

###### イベントリスナーの種類

- `BuildListener`
- `TaskExecutionGraphListener`
- `ProjectEvaluationListener`
- `TaskExecutionListener`
- `TaskActionListener`
- `StandardOutputListener`
- `TaskListener`
- `TestOutputListener`
- `DependencyResolutionListener`

###### イベントリスナーの例

```gradle
class MyTaskActionListener implements TaskActionListener {
    void beforeActions(Task task) {
        println '-- ' + task.name + 'タスクアクション実行前 --'
    }
    void afterActions(Task task) {
        println '-- ' + task.name + 'タスクアクション実行後 --'
    }
}

gradle.addListener(new MyTaskActionListener())
```

```shell-session
$ gradle -I init.gradle help
:help
-- helpタスクアクション実行前 --

Welcome to Gradle 2.8.

To run a build, run gradle <task> ...

To see a list of available tasks, run gradle tasks

To see a list of command-line options, run gradle --help

To see more detail about a task, run gradle help --task <task>
-- helpタスクアクション実行後 --

BUILD SUCCESSFUL
```

イベントリスナーを使うと、そのリスナーが受け持つイベントに対してフックして処理を差し込むことができる。

##### コールバック API

ビルド処理中のイベントをフックして処理を追加できる。

- `beforeProject()`
- `afterProject()`
- `projectsLoaded()`
- `buildFinished()`
- `projectsEvaluated()`
- `settingsEvaluated()`

```gradle
// gradle を省略
settingsEvaluated {
    println '1. settingsEvaluated'
}

projectsLoaded {
    println '2. projectsLoaded'
}

beforeProject {
    println '3. beforeProject'
}

afterProject {
    println '4. afterProject'
}

projectsEvaluated {
    println '5. projectsEvaluated'
}

buildFinished {
    println '6. buildFinished'
}
```

```shell-session
$ gradle -I init-callback.gradle
1. settingsEvaluated
2. projectsLoaded
3. beforeProject
4. afterProject
5. projectsEvaluated
:help

Welcome to Gradle 2.8.

To run a build, run gradle <task> ...

To see a list of available tasks, run gradle tasks

To see a list of command-line options, run gradle --help

To see more detail about a task, run gradle help --task <task>

BUILD SUCCESSFUL
```

### Settings オブジェクト

- 設定スクリプトを表す Gradle のドメインオブジェクト
- マルチプロジェクトだけのものではないが、主な用途がマルチプロジェクトであるため、それを中心とした構造になっている

#### プロパティ

- `gradle`
- `rootDir`
- `rootProject`
- `settings`
- `settingsDir`
- `startParameter`

例えば、マルチプロジェクトの子プロジェクトのビルドスクリプトが `build.gradle` じゃない場合は、スクリプト上でファイル名を取得して設定する必要がある。

```gradle
rootProject.chirdren.each { project ->
    def layerName = project.name.replaceAll('-project', '')
    def buildScriptFile = "${layerName}.gradle"
    project.buildFileName = buildScriptFile

    println "プロジェクト／ビルドスクリプト名:: ${project.name}／${buildScriptFile}"
}
```

```shell-session
$ gradle
プロジェクト／ビルドスクリプト名:: presentation-project／presentation.gradle
プロジェクト／ビルドスクリプト名:: application-project／application.gradle
プロジェクト／ビルドスクリプト名:: domain-project／domain.gradle
プロジェクト／ビルドスクリプト名:: infrastructure-project／infrastructure.gradle
```

#### 主要な API

- `findProject()`
    - 指定したディレクトリまたはファイルパスに一致する `Project` オブジェクトを返す。見つからなかったら `null`
- `project()`
    - 指定したディレクトリまたはファイルパスに一致する `Project` オブジェクトを返す。見つからなかったら例外をスローする
- `include()`
    - 階層型のマルチプロジェクトを構成するプロジェクトを追加
- `includeFlat()`
    - フラット型のマルチプロジェクトを構成するプロジェクトを追加

```gradle
include 'child'
include 'child:grandchild'  // 孫プロジェクトまで含む。セパレータは : を使う
// String の配列を渡してもOK
include 'child', 'child:grandchild'

// includeFlat も同様
includeFlat 'child', 'youngerChild'
```

### ExtensionAware オブジェクト

- Gradle ドメインオブジェクトを他のオブジェクトで拡張可能にするためのドメインオブジェクト
- `extensions` プロパティを持ち、これに独自のプロパティを追加できる

```gradle
class MutableObject {
    private String property

    String getProperty() { return property; }

	void setProperty(String property) {
        this.property = property
	}
}

project.extensions.create('mutable', MutableObject) // プロパティ名と拡張対象クラスを指定
project.mutable.property = 'MutableObjectです。'

task showMutableObject << {
    println project.mutable.property
}


class ImmutableObject {
	private String property

	ImmutableObject(String property) {
	    this.property = property
	}

	String getProperty() { return property; }
}

extensions.create('imutable', ImmutableObject, 'ImmutableObjectです。') // 引数を渡す場合は第3引数以降に追加

task showImmutableObject << {
	println imutable.property
}
```

### ExtraPropertiesExtension オブジェクト

- `Project` や `Task` オブジェクトなどで `ext` の名前で定義されている拡張プロパティの実体
- `properties` というプロパティを持ち、そこにキーバリューで任意のオブジェクトを保持する
- `has()`, `set()`, `get()` の API を提供する

```gradle
// ExtraPropertiesExtension
project.ext.set('property', 'extに追加したプロパティです。')
if (project.ext.has('property') {
    println '拡張プロパティの値:' + project.ext.get('property')
}
```

Groovy のプロパティアクセスや Map アクセス、クロージャでも設定できる。

```groovy
project.ext.property = 'extに追加したプロパティです。'
println '拡張プロパティの値: ' + project.property

project.ext['property'] = 'extに追加したプロパティです。'
println '拡張プロパティの値: ' + project.ext['property']

project.ext {
    prop1 = 'aaa'
    prop2 = 'bbb'
}
println project.ext.prop1 + project.ext.prop2
```

## タスクの記述

### タスクの定義方法

タスクの定義にはいくつか方法がある。

#### タスク定義の基礎

```gradle
// 実行のためのブロックをタスク hello に追加
// （実行フェーズで評価される）
task hello << {
	println name + ': Hello Gradle world!'
}

// タスク hello に対する設定のためのブロック
// （設定フェーズで評価されるため、タスクの実行前に動作して、タスク hello 内では何も動作しない）
task hello {
	println name + ': Hello Gradle world!'
}
```

もし、タスクに `description` を追加したい場合、タスクの定義よりも前に `description` を追加することは出来ない。

```gradle
// これはエラー
task showDescription.description = 'showDescriptionタスクです。'
showDescription << {
	println description
}
```

正しくは、

```gradle
// これは冗長
task showDescription
showDescription.description = 'showDescriptionタスクです。'
showDescription << {
	println description
}

// この方がまだ簡潔
task showDescription << {
	println description
}
showDescription.description = 'showDescriptionタスクです。'
```

設定ブロックであれば、タスクの宣言とプロパティの設定の他に、`doFirst()` か `doLast()` を使ってタスクの処理も一緒に定義できる。

```gradle
task showDescription {
	description = 'showDescriptionタスクです。'
	doLast {
		println description
	}
}
```

ただこれでも波括弧がネストしており、シンプルではなくなっている。タスクの宣言と設定と処理をまとめて記述するには、`<<` を使った宣言で、タスクの引数に設定を渡すと比較的シンプルになる。

```gradle
task showDescription(description: 'showDescriptionタスクです。') << {
	println description
}
```

これに似た定義方法として、タスク名も引数に含める記述もできる。

```gradle
task(showDescription, description: 'showDescriptionタスクです。') << {
	println description
}
```

引数が増えてくると横に長くなるか、改行を増やすことになるので、設定の有無や数などを踏まえて適宜読みやすい記述を選択する。

`TaskContainer` の API を使った記述もできる。

```gradle
// tasks プロパティは Project オブジェクトのプロパティ
tasks.create(name: 'hello') << { println 'Hello' }
tasks.create('hello') << { println 'Hello' }
```

#### Task 型を使用した定義

Gradle が提供する汎用タスクを使って独自タスクを定義することができる。

- `Copy`
- `Delete`
- `Exec`
- `JavaExec`
- `Sync`
- `Tar`
- `Zip`

```gradle
task myCopy(type: Copy) {
	// このブロックは設定ブロック
	from 'original'
	into 'target'
}
```

これはプログラム的な記述で表すと、次のようなイメージ。

```groovy
Copy myCopy = task(myCopy, type: Copy)
myCopy.from('original')
myCopy.into('target')
```

汎用タスクは実行時の処理が実装されているので、基本的には設定ブロックだけで使えるが、タスクの処理を変更したい場合は `Task` オブジェクトの API を用いて独自に拡張することもできる。

```gradle
task myCopyExtension(type: Copy) {
	from 'original'
	into 'target'
	File newFile = file('original/newFile.txt')
	doFirst {
		println 'コピー元のファイルに書き込みします。'
		newFile.write('コピー用のファイルです。', 'UTF-8')
	}
	doLast {
		File copied = file('original/newFile_copied.txt')
		if (newFile.renameTo(copied)) {
			println 'コピーが完了しました。'
		} else {
			println 'コピーできませんでした。'
		}
	}
}
```

#### 動的なタスクの定義

タスク名には文字列も指定でき、動的に文字列を生成することでタスクを動的に定義することもできる。

```gradle
// これだとサーバの種別が増えると辛い
def instances = ['AppServer': '192.0.2.10', 'DBServer': '192.0.2.20', 'MailServer': '192.0.2.30']

task showAppServer << {
	println instances.'AppServer'
}

task showDBServer << {
	println instances.'DBServer'
}
```

```gradle
def instances = ['AppServer': '192.0.2.10', 'DBServer': '192.0.2.20', 'MailServer': '192.0.2.30']

instances.each { serverType, ipAddress ->
	task "show${serverType}" << {
		println ipAddress
	}
}
```

### 依存関係の定義方法

- 依存関係は `dependsOn` というプロパティを使って設定する

```gradle
task todo << {
	println '未着手。'
}
task doing << {
	println '仕掛中。'
}
task done << {
	println '完了。'
}
doing.dependsOn todo
done.dependsOn doing
```

複数のタスクに依存する場合は、リストで指定する。

```gradle
task reviewing << {
	println 'レビュー中。'
}
task done(dependsOn: [doing, reviewing]) << {
	println '完了。'
}
```

また、`dependsOn` プロパティではなく `dependsOn()` メソッドでも記述できる。`dependsOn()` を使用する場合は配列で渡すか、クロージャで指定する。

```gradle
done.dependsOn doing, reviewing
done.dependsOn {
	[doing, reviewing]
}
```

クロージャを使う場合は、`tasks` プロパティを使って特定のタスクを依存関係に追加するような記述も書ける。

```gradle
done.dependsOn {
	tasks.findAll { task -> task.name ==~ /.*ing/ }
}
```

### タスクルールの定義方法

似たようなタスクを異なるパラメータや設定に対して行いたい時、タスクを1つずつ定義するのではなく名前にルールを決めることで簡潔に記述できるようにするのがタスクルール。

例えば、`clean` タスクはビルドで生成した `build/` ディレクトリをまるごと削除するが、テストレポートだけを削除したり、`jar` タスクで生成した JAR ファイルだけを削除したりすることもできる。

```shell-session
$ gradle cleanTest // tests, test-results ディレクトリを削除
$ gradle cleanJar // JAR ファイルを削除
```

タスクルールは `gradle tasks` を実行すると Pattern が表示される。

タスクルールを定義するには、`Project` オブジェクトのプロパティ `tasks` の `addRule()` メソッドを使用する。

```gradle
tasks.addRule('Pattern: show<TASK_NAME>: Show task name.') { taskName ->
	if (taskName.startsWith('show')) {
		task(taskName) << {
			println '*** ' + taskName + ' ***'
		}
	}
}
```

このルールでは `show` で始まっていればなんでもよいことになるので、適当なタスク名でも一致すればタスクとして処理される。

### タスクの制御

- 処理内容の制御
- 実行可否の制御
- 実行順の制御

### タスクの上書き

```gradle
apply plugin: 'java'

task assemble(overwrite: true) {
	println '上書きしました。'
}
```

`overwrite` プロパティを有効にしないと、例外がスローされる。

### タスクの条件実行

例えばあるプロパティの値によってタスクの実行を制御したい場合は、`onlyIf()` を使う。

```gradle
task specialTask << {
	println '特別な処理を実行します。'
}
specialTask.onlyIf {
	buildType == 'partial-build'
}
```

タスク全体ではなく、特定の値や処理結果に異常が発生した時に処理を中断したい場合は、`StopExecutionException` でタスクの実行を制御できる。

```gradle
task skippedIfExceptionOccured << {
	println 'start'
}
skippedIfExceptionOccured << {
	if (process == 'error') {
		throws new StopExecutionException()
	}
}
skippedIfExceptionOccured << {
	println 'end'
}
```

`StopExecutionException` が発生すると、以降のアクションから実行されなくなるが、ビルド自体は正常に終了する。

### タスクの順序付け

- タスクの順序付けは、順序付けられたタスクの両方が実行対象となっている場合だけ、その関係性が有効になる
- 依存関係の場合は、実行対象となっていなくても、依存していれば実行される

タスクの順序付けには以下の2つが用意されている。

- `mustRunAfter()`
- `shouldRunAfter()`

```gradle
task before << {
	println '先に実行する'
}
task after << {
	println '後から実行する'
}
after.mustRunAfter before
```

`shouldRunAfter()` は `mustRunAfter()` と同様に順序付けされるが、順序が無視される場合があり、`mustRunAfter()` ほど厳密な順序付けではない。

- 順序付けによりタスクの循環参照が発生するケース
	- 順序付けが無視される
- タスクが並列実行され、依存関係のあるタスクが分かれて実行されているケース
	- 依存関係にあるタスクが実行されたかどうかにかかわらず、順序付けしたタスクも実行される

### ファイナライザータスク

- 指定されたタスクの実行後に実行されるタスクのことを指す
- `finlaizedBy()` を使ってタスクを指定
- ファイナライザータスクは、対象タスクが実行対称となる場合にタスクグラフに自動的に追加される
- 通常の依存関係では例外やエラーが発生した場合は後続のタスクは実行されないが、ファイナライザータスクは前のタスクで例外やエラーが発生してもタスクが実行される
	- 一方で、対象タスクが依存するタスクが失敗した場合や、対象タスクがスキップされる場合はファイナライザータスクも実行されない
- 使いどころとしては、起動したプロセスをファイナライザータスクで停止したり、ビルド中に作成したリソースを削除するといった用途

```gradle
task normalTask << {
	println name
}
task finalizerTask << {
	println name
}
normalTask.finalizedBy finalizerTask
```

## プラグインの記述

### Gradle におけるプラグイン

- Gradle においてプラグインとは、定型的な形式のプラグインとそれ以外にスクリプトで拡張できるプラグインと、複数の拡張方法がある
- Gradle のプラグインは、「タスク」「プロパティ」「規約」が基本セットとして提供されることが多く、他にも「ソースセット」や「コンフィグレーション」も提供されたりする
- 内部的には、複数のプラグインを拡張しながら形成されている
- プラグインを表す `Plugin` オブジェクトの実体は、`Project`, `Task` などのドメインオブジェクトとは違い、Gradle 本体には含まれず、`Plugin` インタフェースだけが存在しており、`apply()` メソッドだけが定義されている
- プラグイン側で `apply()` を実装して、Gradle がそれらを読み込むことでビルド内で利用できるようになる

### プラグインの適用方法

```gradle
apply plugin: 'java'
// プログラム的に書くと
project.apply(plugin: 'java')
// プラグイン ID が無い時は Plugin インタフェースを実装したクラスを指定
apply plugin: 'org.gradle.api.plugins.JavaPlugin
```

サードパーティや GitHub 上で公開されているプラグインを適用する場合は、`buildscript` ブロックに依存関係と取得元のリポジトリを記述して適用する。

```gradle
buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath 'org.hidetake:gradle-ssh-plugin:0.1.10'
    }
}
apply plugin: 'ssh'
```

Gradle 2.1 から、`plugins` スクリプトブロックが導入され、プラグインの適用方法が変わった。今後はこの記述方法が主流となっていくはず。同時にプラグインポータルサイトも準備されたため、今後はここからプラグインを探せば良い。
ない場合だけ、旧式の記述を行う。

```gradle
// Java プラグイン（プラグイン ID 指定）
plugins {
    id 'java'
}

// Java プラグイン（ネームスペース付き）
plugins {
    id 'org.gradle.java'
}

// 外部プラグイン（バージョン指定付き）
plugins {
    id 'com.example.exampleplugin' version '1.0'
}
```

## マルチプロジェクトでのスクリプトファイルの記述

### レイアウトと記述方法のバリエーション

| マルチプロジェクトのレイアウト | ビルドスクリプトの記述方法 |
| ------------------------------ | -------------------------- |
| 階層型 | 1つのファイルにまとめて記述 |
| 階層型 | プロジェクトごとに記述 |
| フラット型 | 1つのファイルにまとめて記述 |
| フラット型 | プロジェクトごとに記述 |

#### マルチプロジェクトのレイアウト

- 階層型
	- ルートプロジェクトに子プロジェクトがぶら下がる
	- 複数のモジュールを開発しながら、それを統合する Web アプリケーションやバッチアプリケーションに向いている
- フラット型
	- ルートプロジェクトとこプロジェクトが同じ階層に並ぶ
	- デフォルトでは `master` というディレクトリをルートプロジェクトとみなす
	- 独立していたプロジェクトをレイアウトを変えずにマルチプロジェクト化するような場合に向いている

#### ビルドスクリプトの記述方法

- 1つのファイルにまとめて記述
	- ルートプロジェクトのビルドスクリプトにマルチプロジェクトに属する全てのプロジェクト設定を記述する
- プロジェクトごとに記述
	- 子プロジェクトのそれぞれの配下にビルドスクリプトを起き、とうがいプロジェクトに関する設定を記述する
	- 共通なものはルートプロジェクトのビルドスクリプトに記述して、差分のみを個別に書くこともできる

### 階層型の場合のスクリプトファイル

- ルートプロジェクト直下に設定ファイルを置き、`include()` でマルチプロジェクトに含めるプロジェクトを指定する
- プロジェクトパスのセパレータは `:`
- `subprojects` ブロックにサブプロジェクト共通の設定を記述
- `project()` でプロジェクトパスを指定して、そのブロックの中に個別のプロジェクトの設定を記述
- 個別の設定が必要ないプロジェクトは記述しなくても良い（最低限設定スクリプト `settings.gradle` さえあれば動く）
- 他の子プロジェクトへの依存が発生する場合も、`dependencies` ブロックに `project()` を使って指定してやれば良い

```gradle
// settings.gradle
include 'arithmetic-lib', 'arithmetic-main'

// build.gradle
// 共通の設定
subprojects {
    apply plugin: 'java'

    def defaultEncoding = 'UTF-8'
    compileJava.options.encoding = defaultEncoding

    sourceCompatibility = 1.7
    targetCompatibility = 1.7

	javadoc {
		options.links << 'http://docs.oracle.com/javase/jp/7/api/'
    }
}


// ライブラリプロジェクト
project('arithmetic-lib') {}

// アプリケーションプロジェクト
project('arithmetic-main') {
    apply plugin: 'application'

    mainClassName = 'com.example.cli.SimpleCalc'
    applicationName = 'SimpleCalc'

    dependencies {
		compile project(':arithmetic-lib')
    }

    run {
		standardInput = System.in
    }
}
```

### フラット型の場合のスクリプトファイル

- `master` ディレクトリに設定スクリプトを作成し、`includeFlat()` でマルチプロジェクトに含めるプロジェクトを指定
- 階層型と同じように、`subprojects` ブロックで共通の設定を記述
- 各プロジェクトの配下にビルドスクリプトを作成し、個別のプロジェクトの設定を記述する
	- 個別の設定がない場合はビルドスクリプトを作成しなくても動く
- 子プロジェクト内で `gradle` コマンドを実行すると、その子プロジェクトのみビルドが行われる
- 同じプロパティがビルドスクリプトごとに定義されていた場合は、後からロードされる個別のビルドスクリプトの内容で上書きされる

```gradle
// master/settings.gradle
includeFlat 'arithmetic-main', 'arithmetic-lib'

// master/build.gradle
subprojects {
	apply plugin: 'java'

	def defaultEncoding = 'UTF-8'
	compileJava.options.encoding = defaultEncoding

	sourceCompatibility = 1.7
	targetCompatibility = 1.7

	javadoc {
		options.links << 'http://docs.oracle.com/javase/jp/7/api/'
	}
}

// arithmetic-lib/ はビルドスクリプトなし

// arithmetic-main/build.gradle
apply plugin: 'application'

mainClassName = 'com.example.cli.SimpleCalc'
applicationName = 'SimpleCalc'

dependencies {
	compile project(':arithmetic-lib')
}

run {
	standardInput = System.in
}

project(':arithmetic-lib') {
	task show << {
		println project.name + ' show()'
	}
}
```

#### `include()` と `includeFlat()` の違い

- 階層型はルートプロジェクトからのプロジェクトパスで、多重階層のプロジェクトパスを指定できる
	- 多重階層を指定した場合は、すべての階層がプロジェクトになる
- フラット型はルートプロジェクトと同一階層のものがプロジェクト
	- 多重階層のプロジェクトパスを指定すると、そのパス文字列そのものが1つのプロジェクトして認識される

### マルチプロジェクトにおけるタスク

#### マルチプロジェクトでのタスクの指定方法

- 必ずしも対象のプロジェクトディレクトリに移動して実行する必要はない
- `:<プロジェクト名>:<タスク名>` で特定のプロジェクトのタスクだけを実行できる
- 実行ディレクトリはルートプロジェクトに制限されているわけではなく、他のプロジェクトでも同じように実行できる

#### マルチプロジェクトでのタスクの記述

- `subprojects` ブロックで定義したタスクは、子のプロジェクトの定義内で処理を追加することもできる

```gradle
project(':arithmetic-main') {
	commonTask {
		doFirst {
			println '=' * 20 + " ${name} start " + '=' * 20
		}
		doLast {
			println '=' * 20 + " ${name}  end  " + '=' * 20
		}
	}
}
```

#### プロジェクトをまたいだタスクの依存関係の定義

```gradle
project(':arithmetic-main') {
	commonTask {
		dependsOn ':arithmetic-lib:limitedTask'
	}
}
```
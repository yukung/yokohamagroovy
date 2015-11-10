# Gradle の基礎

## ビルドの入力情報

以下の要素が入力として扱われる。

- 初期化スクリプト
	- `Gradle` オブジェクトと対応
- 設定スクリプト
	- `Settings` オブジェクトと対応
- ビルドスクリプト
	- `Project` オブジェクトと対応
- プロパティファイル
- 環境変数／コマンドライン引数
- `buildSrc` プロジェクト

## ビルドの流れ

1. コマンドの解析〜 Gradle の起動
	- コマンドラインオプションから起動モードを決定して起動
2. スクリプトファイルの初期化（初期化フェーズ）
	- 各種ドメインオブジェクトの構築とプロジェクト構成の判定、プロパティの設定などを行う
3. プロジェクトの設定（設定フェーズ）
	- `Task` オブジェクトを生成してタスクグラフを作成する
4. タスクの実行

## アーキテクチャと主要な機能

### 実行基盤を支える仕組みと標準機能

- 実行基盤を支える仕組み
	- 設定の自動ロード
	- プロジェクトの探索
	- タスクグラフ
- 標準機能
	- ファイル操作
	- ロギング

## 設定の自動ロード

- 初期化スクリプトによる設定
	- コマンドライン引数 `-I, (--init-script)` にて初期化スクリプトを指定する
	- `<HOME>/.gradle` に `init.gradle` を配置する
	- `<HOME>/.gradle/init.d/` に `.gradle` の拡張子のファイルを配置する
	- `<GRADLE_HOME>/init.d/` に `.gradle` の拡張子のファイルを配置する
- プロパティファイルによる設定
	- コマンドライン引数 `-D (--system-prop)` でプロパティファイルを指定する
	- `<PROJECT_HOME>/` に `gradle.properties` を配置する
	- `<HOME>/` に `gradle.properties` を配置する

プロパティファイルの同一プロパティの定義が複数の箇所に存在する場合は、ロード順が遅い方の値で上書きされる。

## プロジェクトの探索

- デフォルトでは、`gradle` コマンドを実行するカレントディレクトリがビルド対象のプロジェクトになる
	- シングルプロジェクトではルートプロジェクトでもある
- マルチプロジェクトでは、ルートプロジェクトを基点にして各プロジェクトがその配下に配置される
- プロジェクトの種別判定に設定スクリプト `settings.gradle` を使う。`settings.gradle` は必ずルートプロジェクトの直下に配置しなければならない
- マルチプロジェクトでは必ず `settings.gradle` を用意しなければならない

### 設定スクリプトに基づいた構成の特定

以下の2つの方法で設定スクリプトを読み込むことができる。

- コマンドライン引数の `-c (--settings-file)` オプションでファイルを指定する
- Gradle の規約に従った場所に配置する

2つ目の方法では、以下の順で `settings.gradle` を探す。

1. カレントディレクトリに `settings.gradle` がある場合はそれを設定スクリプトとする
2. カレントディレクトリの親ディレクトリに `settings.gradle` があればそれを設定スクリプトとする
3. カレントディレクトリと同じ階層に `master` ディレクトリがあり、その配下に `settings.gradle` があればそれを設定スクリプトとする

ただし、コマンドライン引数に `-u (--no-search-upward)` オプションが指定されていた場合は、2. と 3. の探索は行わず、カレントディレクトリに設定スクリプトが含まれていない場合はシングルプロジェクトとして扱われる。

## タスクグラフ

### タスクグラフの制約

- 同一のタスクが実行対象タスクとして指定された場合は、1回しか実行されないことが保証されている
	- 複数のタスクから依存されているタスクがあった場合も同様
- タスクが順序付けられている場合
- 明示的に実行対象として指定された時だけ、順序付けが有効になる
- ファイナライザータスクが設定されている場合は、タスクグラフに必ずファイナライザータスクも追加される

## ファイル操作

- ファイルの参照
- ファイルのコピー
- ファイルの削除
- ディレクトリの作成

### 単一ファイルの参照

`file()` メソッドを使う。引数の型によって様々な指定が可能。

- `String`
	- ファイルパスはルートプロジェクトのディレクトリを起点とした相対パス
- `File`
	- ファイルパスは `File` オブジェクトに渡されたパスを起点とした絶対パス
- `URL/URI`
- `Callable` インタフェースを実装したクラス
	- `call()` メソッドの中でファイルパスを指定
- `Closure`
	- クロージャの中でファイルパスを指定

```groovy
File javaFile = file('src/main/java/Main.java')
File dummyTxt = file(new File('src/dummy.txt'))
File indexFile = file(new URL('file:/index.html'))
File indexFile = file(new URI('file:/index.html'))

import java.util.concurrent.Callable
File indexFile = file(new Callable<String>() {
	String call() {
		'/index.html'
	}
}

File indexFile = file { '/index.html' }
```

#### PathValidation によるファイルの検証

`file()` を使う際に合わせて `PathValidation` という列挙型で生成する対象を検証することもできる。値がマッチしない場合は例外が発生する。

```groovy
File existDir = file('src/main/existDir', PathValidation.DIRECTORY)
```

### ファイルコレクションによるファイルの参照

`files()` メソッドを使う。`FileCollection` は、`ConfigurableFileCollection` インターフェースを実装しているが、操作するAPIは親である `FileCollection` が提供しているため、これを見ていく。引数は `file()` が許容する型のオブジェクトを複数渡す形。

```groovy
FileCollection collections = files('file1.txt', 'file2.txt')
collections = files('file1.txt', new File('file2.txt'), new URL('file:/index.html')

List fileList = [new File('file1.txt'), new File('file2.txt')]
// List 型で渡す
collections = files(fileList)
// 配列で渡す
collections = files(fileList as File[])
```

#### ファイルコレクションの変換

```groovy
FileCollection collections = files('file1.txt', 'file2.txt', 'file3.txt')

List list = collections as List
Set set1 = collections as Set
File[] array = collections as File[]

Set set2 = collections.files // Groovy のプロパティアクセス。getFiles() と同じ
```

ファイルコレクションに含まれるファイルが1つだけの場合は、`getSingleFile()` が使える。複数ある場合は例外となる。

```groovy
FileCollection collections = files('file1.txt')
File file = collections.singleFile
```

#### 遅延評価でファイルコレクションを取得する

- `Closure`
- `Callable` インタフェース

上記の引数の場合はファイルの取得タイミングは実際にファイルコレクションにアクセスがあった時となり、定義した時ではないので注意。

```groovy
import java.util.concurrent.Callable

FileCollection orgCollection = null
FileCollection newCollection = files(new Callable<List<File>>() {
	List<File> call() {
		println '-- callable#call --'
		orgCollection as List
	}
})
println '-- before set file --'
orgCollection = files('internal')
println '-- after set file --'
println newCollection.singleFile
```

```shell-session
$ gradle
-- before set file --
-- after set file --
-- callable#call --
/gradle-book/examples/chapter05/file-access/fileCollection/internal
```

#### ファイルコレクションの演算

Groovy の演算子オーバーロードを用いて、コレクション同士を演算子で加算、減算できる。

```groovy
FileCollection collections1 = files('file1.txt')
FileCollection collections2 = collections1 + files('file2.txt')
assert collections2.files.size() == 2

FileCollection collections3 = collection2 - files('files2.txt')
assert collections3.files.size() == 1
```

#### ファイルコレクションのフィルタリング

`filter()` にクロージャを渡すと、条件を満たしたものだけフィルタリングできる。

```groovy
FileCollection collections = files('file1.txt', 'file2.txt', new URL('file:/index.html'))
FileCollection textFiles = collections.filter { collectionFile ->
	collectionFile.name.endsWith '.txt'
}
assert textFiles.files.size() == 2
```

#### 含まれているファイルのパスを連結して出力

`getAsPath()` で、`:` を区切り文字としたファイルパスを連結した文字列が得られる。クラスパス形式でコンフィグレーションとして定義したい場合などに使う。

```groovy
FileCollection libraries = files('lib1.jar', 'lib2.jar')
Stirng librariesPath = libraries.asPath
println librariesPath
```

```shell-session
$ gradle
/gradle-book/examples/chapter05/file-access/fileCollection/lib1.jar:/gradle-book/examples/chapter05/file-access/fileCollection/lib2.jar
```

#### ファイルコレクションの状態を把握

`contains()` や `isEmpty()` を使うと含まれているかや、空かどうかを判定できる。ファイルコレクションが空になってしまった場合に例外を投げる `stopExecutionIfEmpty()` などもある。

```groovy
FileCollection tempCollection = files('file1.txt')
File file1 = tempCollection.singleFile
// ファイルコレクションが空ではない　かつ　'file1.txt' が含まれていたらファイルを削除する
if (!tempCollection.isEmpty() && tempCollection.contains(file1)) {
	tempCollection -= files(file1)
}
// 空だったら、`StopExecutionException` をスローする
tempCollection.stopExecutionIfEmpty()
```

### ファイルツリーによるファイルの参照

`FileCollection` と同様に、ファイルの集合を扱うが、木構造のファイルツリーとして扱う場合は `fileTree()` を使う。ファイルツリーは `ConfigurableFileTree` インタフェースを実装しているが、ファイルツリーへの操作は親である `FileTree` インタフェースで提供されているため、これを見ていく。

#### ファイルツリーの取得

`fileTree()` を使って取得する。引数には基点とするディレクトリパスか、クロージャか `Map` を渡せる。ディレクトリパスの場合はプロジェクトディレクトリからの相対パスを指定する。

```groovy
FileTree sourceTree = fileTree('src')
```

#### ファイルのマッチング

ファイルツリーでは Ant のマッチパターンを利用してツリーの構成を変えることができる。`include()` で対象として含め、`exclude()` で含めない対象を決める。

- `*`
	- 0以上の任意の文字列
- `?`
	- 任意の1文字
- `**`
	- 任意の階層にあるディレクトリとファイル
- `/` または `\` で終わる文字列
	- あとに `**` が続くものとして扱われる。指定ディレクトリ以下のすべてのファイルとマッチ

```groovy
FileTree sourceFiles = fileTree('src')
sourceFiles.each { println it.name }

println '-- include *.java --'
FileTree javaFiles = sourceTree.include('**/*.java')
javaFiles.each { println it.name }

println '-- exclude Something.java --'
FileTree tree = sourceTree.exclude('**/Something.java')
tree.each { println it.name }
```

```shell-session
$ gradle
something.groovy
Main.java
Something.java
prop.properties
-- include *.java --
Main.java
Something.java
-- exclude Something.java --
Main.java
```

#### クロージャによるファイルツリーの取得

クロージャの場合は、基点となるディレクトリパスとクロージャを渡す。

```groovy
FileTree tree = fileTree('src') {
	include '**/*.java'
	exclude '**/Something.java'
}
tree.each { println it.name }
```

#### マップによるファイルツリーの取得

マップの場合は、基点とあるディレクトリパスと一緒に `include()` や `exclude()` などを渡す。

```groovy
FileTree tree = fileTree(dir: 'src', include: '**/*.java', exclude: '**/Something.java')
tree.each { println it.name }
```

#### 条件に一致するファイルツリーの取得

`matching()` というファイルコレクションの `filter()` に似た、フィルタリングのためのメソッドが用意されている。これは `PatternFilterable` というインタフェースを渡すことができる。

`include()` と `exclude()` は `PatternFilterable` で定義されているメソッド。つまり、`ConfigurableFileTree` は `PatternFilterable` を継承している。

```groovy
FileTree tree = fileTree('src')
FileTree javaFiles = tree.matching { include '**/*.java' }
javaFiles.each { println it.name }

FileTree nonJavaFiles = tree.matching { exclude '**/*.java' }
nonJavaFiles.each { println it.name }
```

#### ファイルツリーの探索

ツリーの各ノードを探索する `visit()` を使うと、基点から順に探索してディレクトリやファイルの情報にアクセスできる。`visit()` にはクロージャか `FileVisitor` インタフェースの実装クラスを渡す。クロージャの場合は、クロージャの引数に `FileVisitDetails` インタフェースが渡されるので、それを使ってディレクトリやファイルの情報にアクセスする。

```groovy
FileTree tree = fileTree('src')
tree.visit { fileDetails ->
	println 'name: ' + fileDetails.getName()
	if (fileDetails.isDirectory()) {
		println 'path: ' + fileDetails.getPath()
	} else {
		println 'path: ' + fileDetails.getSize()
	}
}
```

`FileVisitor` インタフェースの実装クラスの場合は、ディレクトリに対するメソッドとファイルに対するメソッドを実装する。

```groovy
FileTree tree = fileTree('src')
tree.visit(new FileVisitor() {
	void visitDir(FileVisitDetails fileDetails) {
		println 'name: ' + fileDetails.getName()
		println 'path: ' + fileDetails.getPath()
	}
	void visitFile(FileVisitDetails fileDetails) {
		println 'name: ' + fileDetails.getName()
		println 'file size: ' + fileDetails.getSize()
	}
})
```

### ファイルのコピー

以下の2つの方法があるが、後者の方法を見ていく。

- `Copy` 型のタスクから独自のタスクを作る
- `copy()` を使う

`copy()` はクロージャを引数とするが、実際のコピーに関する記述はクロージャ内で `CopySpec` というインタフェースを使用して行う。

#### CopySpec とは

`FileCollection` や `FileTree` と同様にファイルのコピーに特化した Gradle が提供するインタフェースの1つ。`Copy` タスクをはじめとする様々なタスクで使われている。

#### コピー元の指定

`from()` を使う。プロジェクトディレクトリからの相対パスでディレクトリを指定する。指定したディレクトリ自体はコピー元には含まれない。

```groovy
from('original')
from 'original' // 括弧省略
```

#### コピー先の指定

`into()` を使う。`from()` 同様プロジェクトディレクトリからの相対パスでディレクトリを指定する。

```groovy
into 'replication'
```

#### コピー対象ファイルのマッチング

- 一致したファイルを含める
    - `include()`
- 一致したものを除外
    - `exclude()`

`CopySpec` インタフェースも `PatternFilterable` インタフェースを継承しているため、`FileTree` と同様 Ant のパターンと同様の指定でパターンマッチングできる。

```groovy
// 含める
include 'example.foo'
include 'example?.bar'
include '**/*.txt'
include 'internal/'
// 可変長引数で受ける
include 'example.foo', 'example?.bar', '**/*.txt', 'internal/'

// 除外
exclude '**/dummy.txt'

// 空のディレクトリを除外
includeEmptyDirs = false // デフォルトは true
```

#### ファイル名のリネーム

`rename()` を使う。引数はクロージャか正規表現を渡す。クロージャの場合は引数に変更前のファイル名が渡される。クロージャから `null` を返すと変更されない。

```groovy
rename { fileName ->
    if (fileName == 'dummy.txt') {
        fileName.replace('dummy', 'renamed-file')
    }
}
```

正規表現を渡した場合は、第1引数の正規表現に一致するファイル名が第2引数のルールにしたがって変更される。

```groovy
rename '(.*)-template.txt', '$1.txt'
```

#### コピー時のファイル編集

コピー時にファイルの内容を編集することもできる。

- 1行ずつ読み込みフィルタリングする
    - `filter()`
- プレースホルダを含んだテンプレートとして読み込んで、与えたパラメータを展開する
    - `expand()`

`filter()` にはクロージャか `java.io.FilterReader` のサブクラスを渡せる。クロージャの場合は引数に1行毎の文字列が渡される。

```groovy
filter { line ->
    line.replaceAll 'VALUE_OBJECT_NAME', 'ConcreteVo'
}
```

`java.io.FilterReader` のサブクラスを渡した場合は、クラスとプロパティがパラメータになる。Ant が持つ `java.io.FilterReader` のサブクラスを利用すると便利。

```groovy
filter org.apache.tools.ant.filters.TabsToSpaces, tablength: 4
```

`expand()` は、フィルタリング対象のファイルを Groovy の `SimpleTemplateEngine` のテンプレートとして読み込み、テンプレート変数を引数に渡したパラメータで置換できる。

```java
// template.java
package ${packageName};

/**
 * ${className}。
 * Gradleで自動生成しています。
 */
public class ${className} {
<% fields.each { type, fieldName -> %>
    private ${type} ${fieldName};<% } %>

    public void ${className}() {}
<% fields.each { type, fieldName ->
	String methodName = fieldName.capitalize() %>
    public ${type} get${methodName}() {
        return ${fieldName};
    }
    public void set${methodName}(${type} ${fieldName}) {
        this.${fieldName} = ${fieldName};
    }<% } %>
}
```

```groovy
// build.gradle
String targetName = 'SimpleBean'
copy {
    from 'template'
    into 'output'

	expand packageName:'com.example.bean', className:targetName,
	      fields:['String':'name', 'int':'value']

	rename 'template.java', "${targetName}.java"
}
```

```java
// SimpleBean.java
package com.example.bean;

/**
 * SimpleBean。
 * Gradleで自動生成しています。
 */
public class SimpleBean {

    private String name;
    private int value;

    public void SimpleBean() {}

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public int getValue() {
        return value;
    }
    public void setValue(int value) {
        this.value = value;
    } 
}
```

注意点として、`filter()`, `expand()` は読み込むファイルのエンコーディングが指定できないため、JavaVM のデフォルトエンコーディングが使用される。Windows 上で日本語を扱うケースなどは文字化けしてしまうため、 `JAVA_OPTS` 環境変数にデフォルトエンコーディングを設定するなどして回避する。

#### `CopySpec` の入れ子

`CopySpec` インタフェースは入れ子にして使うこともできる。`from()` か `into()` にクロージャを渡すことで、内部でさらに `CopySpec` インタフェースの設定ができる。

```groovy
copy {
    into 'replication'
    exclude '**/dummy.txt'
    from('original') {
        exclude 'sub1/', 'sub2/'
    }
    into('internal') {
        from 'original/sub1', 'original/sub2'
    }
}
```

```
.
├── build.gradle
├── original
│   ├── internal
│   │   ├── dummy.txt
│   │   └── inner.txt
│   ├── sub1
│   │   ├── dummy.txt
│   │   └── sub1.txt
│   └── sub2
│       ├── dummy.txt
│       └── sub2.txt
└── replication
    └── internal
        ├── inner.txt
        ├── sub1.txt
        └── sub2.txt
```

### ファイルの削除

ファイルのコピーと同様、以下の2つの方法があるが、後者の方法を見ていく。

- `Delete` 型のタスクから独自のタスクを作る
- `delete()` を使う

`delete()` は引数に削除対象のファイルまたはディレクトリを指定する。可変長引数となっているので複数指定もできる。

```groovy
delete 'target.txt'
// 複数
delete 'target1.txt', 'target2.txt', 'target3.txt'
```

### ディレクトリの作成

`mkdir()` を使う。メソッドのパラメータとなるパスはデフォルトではプロジェクトディレクトリからの相対パスとなる。作成するディレクトリの親ディレクトリが存在しなければ、遡って必要な親ディレクトリも一緒に作成する。

また、プロジェクト外にもディレクトリを作成することもできる。

```groovy
mkdir 'parent/child/grandchild'
mkdir '../outOfTheProject'
mkdir '/gradle-book/absolutePathDir'
```

また、ファイルを作成するメソッドはないが、`File` クラスを使って作成することができる。この際の `File` クラスは `java.io.File` ではなく、Groovy拡張の GDK の `File` クラス。

```groovy
File newFile = file('newFile.txt')
newFile.write('ビルドスクリプトからファイルの作成を行います。', 'UTF-8')
```

## ロギング

### Gradle でのロギング

Gradle は SLF4J を拡張したロガーを包含している。Gradle のロガーには6つのログレベルがある。

| ログレベル | 出力情報 | コマンドラインオプション |
| --------- | ------- | ------------------- |
| `ERROR` | エラーメッセージ | なし |
| `QUIET` | 重要なメッセージ | `-q (--quiet)` |
| `WARNING` | 警告メッセージ | なし |
| `LIFECYCLE` | ビルドの進行状況を示すメッセージ | なし（デフォルト）|
| `INFO` | インフォメーションメッセージ | `-i (--info)` |
| `DEBUG` | デバッグメッセージ | `-d (--debug)` |

標準出力（`println()`）はそれ自体のログレベルがない。デフォルトではビルドスクリプト上の標準出力は `QUIET` レベルにリダイレクトされる。標準エラー出力（`System.err.println()`）は `ERROR` レベルにリダイレクトされる。

一時的に局所のログを出力したい場合は標準出力をつかっても良いが、恒久的にログを出す場合は `logger` プロパティを使うべき。

### `logger` プロパティを使用したロギング

Gradle はビルドスクリプト中の任意の場所で `logger` プロパティを使ってログ出力できる。

```groovy
logger.error 'エラーメッセージ'
logger.quiet '重要なメッセージ'
logger.warn '警告メッセージ'
logger.lifecycle 'ビルドの進行状況を示すメッセージ'
logger.info 'インフォメーションメッセージ'
logger.debug 'デバッグメッセージ'
```

### 外部ロギングフレームワークとのマッピング

Gradle ではビルドスクリプト上で他の Java のロギングフレームワークを利用することもできる。利用した場合は標準出力と同様に Gradle のロガーにリダイレクトされる。

#### Apache Log4j/Log4j2

| Gradle のログレベル | Log4j のログレベル |
| ----------------- | ---------------- |
| `ERROR`           | `FATAL/ERROR`    |
| `QUIET`           | なし              |
| `WARNING`         | `WARN`           |
| `LIFECYCLE`       | なし              |
| `INFO`            | `INFO`           |
| `DEBUG`           | `DEBUG`          |

#### Apache Commons Logging

| Gradle のログレベル | commons-logging のログレベル |
| ----------------- | ---------------- |
| `ERROR`           | `FATAL/ERROR`    |
| `QUIET`           | なし              |
| `WARNING`         | `WARN`           |
| `LIFECYCLE`       | なし              |
| `INFO`            | `INFO`           |
| `DEBUG`           | `DEBUG`          |

#### JDK 標準ロギング API（`java.util.logging`）

| Gradle のログレベル | commons-logging のログレベル |
| ----------------- | ---------------- |
| `ERROR`           | `SEVERE`         |
| `QUIET`           | なし              |
| `WARNING`         | `WARNING`        |
| `LIFECYCLE`       | なし              |
| `INFO`            | `INFO/CONFIG`    |
| `DEBUG`           | `FINE`           |

`FINER` と `FINEST` レベルは出力されない。

### ログレベルマッピングの変更

`logging` プロパティを利用して標準出力や標準エラー出力のログレベルを変更できる。

```groovy
// 標準エラー出力を ERROR から INFO に変更
logging.captureStandardError LogLevel.INFO
/// 標準出力を QUIET から INFO に変更
logging.captureStandardOutput LogLevel.INFO
```


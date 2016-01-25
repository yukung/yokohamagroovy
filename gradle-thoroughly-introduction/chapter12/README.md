# アーカイブの作成とファイルの公開

## Gradle におけるアーカイブ

Gradle には ZIP や TAR などのアーカイブを作成する**アーカイブタスク**が組み込まれている。例えば ZIP ファイルを作成するには、`Zip` タスクを使って次のように記述する。

```gradle
task myZip(type: Zip) { // myZip というアーカイブタスクをビルドに追加
    baseName = "project-docs" // zip ファイル名
    // docs エントリに src/dist ディレクトリから html, css, js ファイルを全て追加
    into('docs') {
        from ('src/dist') {
            include "**/*.html", "**/*.css", "**/*.js"
        }
    }
}
```

アーカイブタスクは、様々な組み込みプラグインで使用されている基礎的なタスク。

`jar` タスクも、`zip` タスクと同様に `Java` プラグインが内部的に `jar` タスクを追加して `jar` ブロックで設定できるようにしている。`war` タスクも同様。

組み込みタスクで提供していないアーカイブタスクは、自分で記述することで可能となる。例えば以下の様なタスクは自分で記述することで実現できる。

* ソースコードを固めた JAR ファイルを作成する
* Javadoc などのドキュメントをアーカイブする

## アーカイブタスク

アーカイブタスクは全て `AbstractArchiveTask` を継承した、以下の様なクラスの継承関係となっているため、下位のタスクは上位のタスクの性質を引き継ぐ。そのため例えば `Jar` タスクで行える設定は `War` タスクでも利用できる。

* `AbstractArchiveTask`
    * `Tar`
    * `Zip`
        * `Jar`
            * `War`

### アーカイブに含めるファイルの指定、出力先のアーカイブエントリの指定

| オプション | 説明 |
| ---------- | ---- |
| `from` | 含めるディレクトリの指定（指定したディレクトリそのものは含まれない） |
| `into` | アーカイブ内に出力するディレクトリの指定 |
| `include` | 含めるファイルの指定。Ant のファイル指定子の記法が使える。 |
| `exclude` | 除外するファイルの指定。
| `includeEmptyDirs` | からディレクトリをアーカイブに含めるかどうか |

```gradle
task docsZip(type: Zip) {
    into("userguide") {
        from("build/docs") {
            include "**/*.css", "**/*.html"
            rename { originalName ->
                if (originalName == "index.html")
                    return "index-${new Date().format("yyyyMMddHHmm")}.html"
            }
            exclude "single.html"
            includeEmptyDirs = false
        }
        from("src/test") {
            include "**/*.groovy"
            filter { line ->
                line.replaceAll(/yukung/, 'Yusuke Ikeda')
            }
            filter org.apache.tools.ant.filters.TabsToSpaces, tablength: 2
        }
    }
    baseName = "project-src"
    from("src/groovy") {
        include "**/*.groovy"
    }
}
```

また、`exclude` についてはデフォルトで除外されるファイルが決まっており、OS の隠しファイルやバージョン管理システムの管理ディレクトリなどは指定しなくてもデフォルトで除外されている。もし、含めたい場合は組み込みの API を使って設定する。

```gradle
org.apache.tools.ant.DirectoryScanner.removeDefaultExclude("**/.gitignore")
```

### CopySpec と入れ子のアーカイブ設定

`from()`, `into()`, `include()`, `includeEmptyDirs` などは、コピータスクや `processResources` などでも設定できた。これは、アーカイブタスクも同様に `CopySpec` インタフェースを実装しているため。

また、`CopySpec` に従ったタスクは入れ子にすることができるため、アーカイブタスクも設定を入れ子にできる。

### アーカイブ時にファイルをリネームする

`CopySpec` の `rename()` を使う。

```gradle
rename { originalName ->
    if (originalName == "index.html")
        return "index-${new Date().format("yyyyMMddHHmm")}.html"
}
rename(/THEME_${docTheme}-(.*\.css)/, '$1') // THEME_BLUE-main.css -> main.css にリネーム
```

### アーカイブ時にファイルの内容を変更する

`CopySpec` の `filter()` や `expand()` を使う。

```gradle
filter { line ->
    line.replaceAll(/yukung/, 'Yusuke Ikeda')
}
// タブをスペースに変換
filter org.apache.tools.ant.filters.TabsToSpaces, tablength: 2
// テンプレート展開
// アーカイブ時に読み込まれたファイルの <%= version %> というプレースホルダをプロジェクトバージョンの値で置換する
expand version: project.version
```

### アーカイブファイルの出力先設定

アーカイブの出力先やファイル名は、`baseName` などのタスクプロパティを元に決定される。

```gradle
// <プロジェクトディレクトリ>/project-src.zip に出力
baseName = "project-src"
from("src/groovy") {
    include "**/*.groovy"
}
```

`baseName` の他に以下が設定でき、それによってアーカイブファイル名が決定される。

* `baseName`
* `destinationDir`
* `appendix`
* `version`
* `classifier`
* `extension`

```
<destinationDir>
  └ <baseName>-<appendix>-<version>-<classifier>.<extension>
```

また、`archiveName` が設定されている場合は上記の指定は無視して、`archiveName` を優先する。

```
<destinationDir>
  └ <archiveName>
```

#### プラグインによるアーカイブアスクの自動設定

Gradle の内部プラグインである `Base` プラグインを適用すると（多くの組み込みプラグインから内部的に利用されている）、プロジェクトに存在する全てのアーカイブタスクに以下の様なデフォルト設定が適用される。

* `destinationDir`
    * `<プロジェクトディレクトリ>/build/distributions`
* `baseName`
    * プロジェクト名
* `version`
    * プロジェクトのバージョン

## アーカイブ形式固有の設定

### ZIP ファイル

`Zip` タスクを使用する。

```gradle
task docsZip(type: Zip) {
  // タスクの設定（zipファイル出力先、圧縮レベルなど）
  baseName = "project-docs"
  entryCompression = ZipEntryCompression.STORED

  // アーカイブの構成
  // 「docs」エントリに「src/dist」ディレクトリとembedded.zipからhtml、css、jsファイルを全て追加
  into ("docs") {
    from (zipTree("src/dist/embedded.zip")) {
      include "**/*.html", "**/*.css", "**/*.js"
    }
    from ("src/dist") {
      include "**/*.html", "**/*.css", "**/*.js"
    }
  }
}
```

### TAR ファイル

`Tar` タスクを使用する。

```gradle
task docsTar(type: Tar) {
  baseName = "project-docs"
  compression = Compression.BZIP2 // 圧縮方法の設定

  // アーカイブの構成
  into ("docs") {
    from (zipTree("src/dist/embedded.zip")) {
      include "**/*.html", "**/*.css", "**/*.js"
    }
    from ("src/dist") {
      include "**/*.html", "**/*.css", "**/*.js"
    }
  }
}
```

### JAR ファイル

`Jar` タスクを使用する。`Zip` タスクを継承しており、基本的には `Zip` タスクと同じように使用できる。違いは、JAR ファイルに含めるマニフェストファイルを設定できること。

```gradle
version = 1.0

task sourcesJar(type: Jar) {
  // タスクの設定（jarファイル出力先など）
  baseName = "my-library"
  appendix = "sources"
  destinationDir = file("build")

  // アーカイブの構成
  from("src/main/java") {
    include "**/*.java", "**/*.groovy", "**/*.xml"
  }
  // マニフェストファイルの設定
  manifest {
    attributes("Built-By": "Gradle", "Implementation-Version": project.version)
  }
}
```

### WAR ファイル

`War` タスクを使用する。`Jar` タスクの機能に加えて、`web.xml` や `WEB-INF` ディレクトリ、`WEB-INF/lib` に JAR ファイルを加える機能が追加されている。

```gradle
version = 1.0

task myWar(type: War) {
    // タスクの設定（ファイル出力先など）
    baseName = "my-war"
    destinationDir = file("build")

    // アーカイブの構成
    from("webapp") {
      include "**/*.html", "**/*.js", "**/*.css"
    }
    // ライブラリJARの設定
    classpath fileTree("lib")
    // WEB-INFの構成
    webInf {
      into("classes") {
        from ("out") {
          include "**/*.class"
        }
      }
    }

    webXml file("web.xml")

    manifest {
     attributes("Built-By": "Gradle", "Implementation-Version": project.version)
   }
}
```

* `classpath()`
    * `FileCollection` 型の引数を渡すと、その内容を `WEB-INF/lib` 直下に格納する。引数で指定した対象のディレクトリ構成は無視されて、フラットな状態で `WEB-INF/lib` に配置される
* `webInf()`
    * `CopySpec` に従ったクロージャを渡すことで、`WEB-INF` ディレクトリの内容を自由に設定できる
* `webXml()`
    * 好きな `web.xml` ファイルを `WEB-INF` 直下に出力できる

また、自分で `War` タスクを定義しなくても、`War` プラグインを適用することで予め一般的な設定が適用された `war` という名前の `War` タスクが作成される。

## Distribution プラグイン

`Distribution` プラグインを使うと、自分でアーカイブタスクを書かなくても簡単にアーカイブを作成できる。

以下のタスクがプロジェクトに追加される。

| タスク名 | タスクの種類 | タスク実行時の処理 |
| -------- | ------------ | ------------------ |
| `distZip` | `Zip` | `<buildDir>/distributions/<project.name>-<project.version>.zip` に ZIP ファイルが作成される |
| `distTar` | `Tar` | `<buildDir>/distributions/<project.name>-<project.version>.tar` に TAR ファイルが作成される |
| `installDist` | `Sync` | `<buildDir>/install/main` にアーカイブを展開してコピーする |

```gradle
apply plugin: 'distribution'
distributions {
    main {
        baseName = 'new-archive-name'
        contents {
            from { 'src/readme' }
        }
    }
}
```

## Gradle によるファイルの公開

作成したアーカイブをリモートもしくはローカルのリポジトリに公開し他のシステムから利用できるようにする方法を以下に記述。Gradle でファイルを公開するには、組み込みプラグインの `Maven Publish` プラグインおよび `Ivy Publish` プラグインを使う。

それぞれのプラグインは `Publishing` プラグインを継承しているため、基本的な使い方は変わらず、固有の設定箇所だけが異なっている設計になっている。

## モジュールの定義

Gradle が取り扱う Maven/Ivy リポジトリは、**モジュール**という単位でファイルを扱う。モジュールには、アーティファクトと呼ばれるモジュールの実体ファイルと、モジュールに関する情報が記述されたメタデータファイル（POM や `ivy.xml`）が含まれている。メタデータに記述されるのはモジュールのグループ名やバージョン、開発者の連絡先、依存関係など。

Gradle でファイルを公開する際も、次のような流れでファイルを公開することになる。

1. モジュールを定義し
2. 定義したモジュールにファイルをアーティファクトとして登録し
3. アーティファクトに関するメタデータを編集し
4. そのモジュールをリポジトリに公開する

Gradle では、ソフトウェアコンポーネントという概念が存在し、プロジェクトにこの概念を自動的に追加するプラグインがいくつか存在する。ソフトウェアコンポーネントは、アーティファクトとその依存関係に関する情報が含まれており、これをモジュールに登録することで依存関係が一括で指定できる。これにより上記 2, 3 にかかる手間を減らすことが出来る。自分でスクラッチに書くことも可能だが、ソフトウェアコンポーネントを使って登録したほうが楽なのでこれを使ったほうが良い。

#### Maven Publish プラグイン、Ivy Publish プラグインの制限

Gradle 1.4 から追加されたプラグインだが、それ以前は `Maven` プラグインを使っていた。これは将来的に置き換えられるはずだが、旧版のプラグインで出来ていたことが新版ではできなくなっていることがある。そのため場合によっては `Maven` プラグインを使わなければならない場合もある。

`Maven/Ivy Publish` プラグインで出来ないことの一つが、対応プロトコルで、例えば `Maven Publish` プラグインは `file` プロトコルと `http(s)` プロトコルでしかファイルをアップロード出来ない。（`Ivy Publish` プラグインは Gradle 2.0 から `SFTP` でもアップロードできるようになった）

`WebDAV` や `SFTP` などでリポジトリにファイルを転送することは新版のプラグインでは出来ないので、これらのプロトコルを使わざるをえない場合は、`maven` プラグインを使うしかない。`Nexus`, `Artifactory`, `Archiva` といった一般的なリポジトリツールであれば `http(s)` は対応している。

Maven Central リポジトリは、ファイルに署名する必要がある関係で `Maven Publish` プラグインで直接公開することは出来ない。そのため JCenter Maven リポジトリをゲートウェイに使って転送する方法がある。

### ソフトウェアコンポーネントを登録する

ソフトウェアコンポーネントはアーティファクトとそれが依存するファイルをグループ化する概念。特定のプラグインを適用することで自動的にそのビルドに追加される。

例えば、`Java` プラグインを適用した場合、ビルドによって作成された JAR ファイルと、JAR ファイルの実行に必要な依存関係をまとめた `java` という名前のソフトウェアコンポーネントがビルドに追加される。

追加されたソフトウェアコンポーネントは次のように `components.<ソフトウェアコンポーネント名>` でアクセスできる。

```gradle
apply plugin: "java"

repositories {
  mavenCentral()
}

dependencies {
  runtime "org.apache.commons:commons-lang3:3.3.1"
}

task showSoftwareComponent << {
  // ソフトウェアコンポーネント「java」の情報を出力

  println "---Artifacts---"
  for (a in components.java.usages.artifacts) {
    println a.file //-> <プロジェクト>/build/libs/<プロジェクト名>.jar
  }

  println "---Dependencies---"
  for (d in components.java.usages.dependencies) {
    println d //-> "org.apache.commons:commons-lang3:3.3.1"に関する情報
  }
}
```

このソフトウェアコンポーネントを、ビルドが公開するモジュールに登録するには、`Maven Publish` プラグインまたは `Ivy Publish` プラグインを適用し、`publishing` ブロックを使用する。

```gradle
apply plugin: "java"
apply plugin: "maven-publish" // Ivyリポジトリへ公開する場合はivy-publishを適用

group = "com.example"
version = 0.1

repositories {
  mavenCentral()
}

dependencies {
  runtime "org.apache.commons:commons-lang3:3.3.1"
}

publishing { // 公開設定
  publications {
    mod1(MavenPublication) { // <mod1>は任意のモジュール識別名。Ivyの場合はIvyPublication
      from components.java // ソフトウェアコンポーネント「java」をモジュールに登録
    }
  }
}
```

```gradle
apply plugin: "java"
apply plugin: "ivy-publish" // Ivy Publishプラグインを適用

// ...

group = "com.example"
version = 0.1

repositories {
  mavenCentral()
}

dependencies {
  runtime "org.apache.commons:commons-lang3:3.3.1"
}

publishing { // 公開設定
  publications {
    mod1(IvyPublication) { // <mod1>は任意のモジュール識別名
      from components.java // ソフトウェアコンポーネント「java」を公開対象に指定する
    }
  }
}
```

上記の例はまだ公開先リポジトリの指定をしていないので、実際に公開することはできないが、`Maven Publish` プラグインの場合はローカルリポジトリへインストールできる。`publish<モジュール識別名>MavenLocal` というタスクを実行すると行える。`Maven/Ivy Publish` プラグインは、 `publishing` ブロックに定義したモジュール識別名に応じて動的にタスクを追加する。

```console
$ gradle publishMod1PublicationToMavenLocal
```

上記を実行すると、`~/.m2/repository` 配下にグループ名、プロジェクト名、バージョンごとのディレクトリに JAR と POM ファイルが出力される。

現在組み込みのソフトウェアコンポーネントは Java プラグインが追加する `java` と、War プラグインが追加する `web` の2つのみ。

### アーティファクトを登録する

任意のファイルをモジュールのアーティファクトとして直接指定することで、外部に公開するファイルを追加することができる。この機能は、ソフトウェアコンポーネントを提供するプラグインを使用していない場合や、ソフトウェアコンポーネントのアーティファクト以外のファイルを追加で公開したい時に使う。以下は Java プラグインを使用していない場合の定義例。

```gradle
apply plugin: "maven-publish" // Maven Publishプラグインを適用

group = "com.example"
version = 0.1

publishing {
  publications {
    docs(MavenPublication) {
      artifactId 'project-docs-list'  // 未指定の場合は project.name が使用される
      artifact('my-docs-index.htm') { // artifact()メソッドでファイルを直接アーティファクトに指定
        extension 'html'
      }
    }
  }
}
```

ファイルをモジュールのアーティファクトとして指定するには `publications` ブロックで `artifact()` を使用する。`artifact()` の用途として一番良く挙げられる例は、Java ライブラリとともに公開されることが多いソースコードの JAR ファイルや Javadoc の JAR ファイルを公開するパターン。

```gradle
apply plugin: "java"
apply plugin: "maven-publish" // Ivyリポジトリへ公開する場合はivy-publishを適用

//...

group = "com.example"
version = 0.1

repositories {
  mavenCentral()
}

dependencies {
  runtime "org.apache.commons:commons-lang3:3.3.1"
}

task sourceJar(type: Jar) {
  classifier 'sources'
  from sourceSets.main.allJava // ソースセットを入力ファイルに設定
}
task javadocJar(type: Jar, dependsOn: javadoc) {
  classifier 'javadoc'
  from javadoc.destinationDir // Javadocのターゲットディレクトリを入力ファイルに設定
}

publishing {
  publications {
    mod1(MavenPublication) { // <mod1>は任意のモジュール識別名
      from components.java // ソフトウェアコンポーネント「java」を公開対象に指定する
      // artifact()メソッドで公開するアーカイブやファイルを指定する
      artifact(sourceJar) // タスクを指定すると、タスクによる出力ファイルが公開される
      artifact(javadocJar)
    }
  }
}
```

Ivy でも同様。

```gradle
apply plugin: "java"
apply plugin: "ivy-publish" // Ivy Publishプラグインを適用

//...

group = "com.example"
version = 0.1

repositories {
  mavenCentral()
}

dependencies {
  runtime "org.apache.commons:commons-lang3:3.3.1"
}

task sourceJar(type: Jar) {
  classifier 'sources'
  from sourceSets.main.allJava // ソースセットを入力ファイルに設定
}
task javadocJar(type: Jar, dependsOn: javadoc) {
  classifier 'javadoc'
  from javadoc.destinationDir
}

publishing {
  publications {
    mod1(IvyPublication) { // <mod1>は任意のモジュール識別名
      from components.java // ソフトウェアコンポーネント「java」を公開対象に指定する
      // artifact()メソッドで公開するアーカイブやファイルを指定する
      artifact(sourceJar) { // タスク名を指定すると、タスクによる出力ファイルが公開される
        type 'source'
      }
      artifact(javadocJar) {
        type 'doc'
      }
    }
  }
}
```

`artifact()` を使うと、プラグインで指定されたソフトウェアコンポーネントだけでなく、任意のアーティファクトをモジュールに追加してプロジェクトが公開するファイルを細かく調整できる。

#### `artifact()` について

`artifact()` の引数には、アーティファクトの `classfier` や `extension` といった属性を保持する `PublishArtifact` というクラスのインスタンスを渡すことができるが、自分でこのクラスのインスタンスを作って渡すことはあまりない。`artifact()` には他にも次のインスタンスを渡せる。

* アーカイブタスク
    * 渡せるタスクは、`AbstractArchiveTask` を継承したタスク、つまり `Zip` や `Jar` タスク。
    * `classifier` および `extension` はアーカイブタスクのタスクプロパティから取得される
* `File` インスタンス
    * 正確には Gradle 組み込みの `file()` によって `File` インスタンスに変換できるオブジェクトであれば渡すことができる。
    * `classfier` および `extension` はファイル名から決定される

これらのインスタンスを渡した場合、上記のようにアーティファクトの属性は渡したインスタンスから自動的に設定される。しかしモジュールのアーティファクトとして公開するということで、さらにアーティファクトの属性を調整したいことがある。以下のように `artifact()` の引数にクロージャを渡すことで、アーティファクトの属性を調整できる。

```gradle
artifact('my-docs-index.htm') {
    extension 'html'
}
```

Maven モジュール、つまり `MavenPublication` を使って定義したモジュールの場合、`artifact()` で調整できる属性は `classifier` と `extension` のみ。

Ivy モジュール、つまり `MavenPublication` を使って定義したモジュールの場合は、`artifact()` のクロージャでさらに `name`, `type`, `conf` を指定できる。


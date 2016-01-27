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

Ivy モジュール、つまり `IvyPublication` を使って定義したモジュールの場合は、`artifact()` のクロージャでさらに `name`, `type`, `conf` を指定できる。

### メタデータのカスタマイズ

モジュールのメタデータ、すなわち `pom.xml` や `ivy.xml` をカスタマイズすることができる。基本的にこれらのメタデータはプロジェクトのプロパティから自動的に作成されるが、ビルドスクリプト上で細かくカスタマイズすることもできる。

#### POM ― Maven Publish プラグインの場合

POM を変更するには、`MavenPublication` ブロック内でプロパティを設定する。POM のうち、`artifactId`, `groupId`, `version` に関してはデフォルト値としてプロジェクトのプロパティが設定されるが、変更する場合も `MavenPublication` ブロックでプロパティを設定するだけ。それ以外の POM 要素を変更するには、`pom` というプロパティを通じて直接 XML を編集する。

```gradle
apply plugin: "java"
apply plugin: "maven-publish"

group = "com.example"
version = 0.1

// ...

repositories {
  mavenCentral()
}

dependencies {
  runtime "org.apache.commons:commons-lang3:3.3.1"
}

publishing { // 公開設定
  publications {
    mod1(MavenPublication) {
      from components.java
      artifactId = "my-maven-mod1" // 未指定の場合は project.name が使用される
      groupId = "com.example.maven" // 未指定の場合は project.group が使用される
      version = "0.1-a" // 未指定の場合は project.version が使用される

      pom.withXml { xml ->
        final myPom = {
          packaging "jar"
          nama "My Sample Project"
          description "サンプルプロジェクト バージョン$version"
          licenses {
            license {
              name "The Apache Software License, Version 2.0"
              url "http://www.apache.org/licenses/LICENSE-2.0.txt"
              distribution "repo"
            }
          }
        }
        // description などプロジェクトのプロパティと被ったときの対策
        myPom.resolveStrategy = Closure.DELEGATE_FIRST

        xml.asNode().children().last() + myPom
      }
    }
  }
}
```

`pom.withXml()` に POM を変更するためのクロージャを渡すことができる。操作は Groovy の XML 操作（`MarkupBuilder`）なので、その知識があれば問題ない。逆に言うとタグ名などを間違ってしまってもそのまま出力されるため、注意が必要。

クロージャに対して `resolveStrategy` プロパティを `Closure.DELEGATE_FIRST` に設定すると、プロパティ名が Gradle のものと被った時にクロージャ側のものを優先するように設定できる。

#### ivy.xml ― Ivy Publish プラグインの場合

Ivy の場合も Maven と同様、`IvyPublication` ブロック内でプロパティを設定するだけでよい。ivy.xml のうち、`info` 要素の `module.organisation`, `revision` に関しては `IvyPublication` ブロック内でプロパティを設定するだけで変更できる。それ以外の要素を変更するには、`descriptor` というプロパティを通じて直接 XML を編集する。

```gradle
apply plugin: "java"
apply plugin: "ivy-publish"

group = "com.example"
version = 0.1

// ...

repositories {
  mavenCentral()
}

dependencies {
  runtime "org.apache.commons:commons-lang3:3.3.1"
}

publishing { // 公開設定
  publications {
    mod1(IvyPublication) {
      from components.java
      module = "my-maven-mod1" // 未指定の場合は project.name が使用される
      organisation = "com.example.maven" // 未指定の場合は project.group が使用される
      revision = "0.1-a" // 未指定の場合は project.version が使用される

      descriptor.withXml { xml ->
        final infoNode = xml.asNode().info[0]
        final licenseNode = {
          license {
            name "The Apache Software License, Version 2.0"
            url "http://www.apache.org/licenses/LICENSE-2.0.txt"
          }
        }
        licenseNode.resolveStrategy = Closure.DELEGATE_FIRST

        infoNode.appendNode("description", "サンプルプロジェクト バージョン$version")
        infoNode.description + licenseNode
      }
    }
  }
}
```

## モジュールの公開

### 公開先リポジトリの設定

公開先リポジトリを設定するには、Maven Publish プラグインまたは Ivy Publish プラグインを適用し、`publishing/repositories` ブロックで設定する。以下に例を挙げる。

```gradle
apply plugin: "java"
apply plugin: "maven-publish" // Maven Publishプラグインを適用

//..

group = "com.example"
version = 0.1

repositories {
  mavenCentral()
}

dependencies {
  runtime "org.apache.commons:commons-lang3:3.3.1"
}

task sourceJar(type: Jar) {
  from sourceSets.main.allJava // ソースセットを入力ファイルに設定
}

publishing { // 公開設定
  publications {
    mod1(MavenPublication) { // <mod1>は任意のモジュール識別名
      from components.java // ソフトウェアコンポーネント「java」を公開対象に指定する
      // artifact()メソッドで公開するアーカイブやファイルを指定する
      artifact sourceJar {
        classifier "sources"
      }
    }
  }

  repositories {
    maven {
      name "local1" // リポジトリ名。省略した場合は「maven」
      url "build/repo1" // ディレクトリを指定
    }
    maven {
      name "remote1" // リポジトリ名。省略した場合は「maven」
      url "http://localhost:8081/artifactory/repo1" // リポジトリのURLを指定
    }
  }
}
```

### 公開に使うタスク

Maven Publish プラグインおよび Ivy Publish プラグインは、`publishing` ブロックで定義されたモジュールとリポジトリに応じて、モジュール公開用のタスクを動的にプロジェクトに追加する。

```
publish<モジュール識別名>PublicationTo<リポジトリ名>Repository
```

というタスクが追加される。先の例では

* `publishMod1PublicationToLocal1Repository`
* `publishMod1PublicationToRemote1Repository`

というタスクが追加され、それぞれ `repositories` ブロック内で定義した `local1`, `remote1` のリポジトリの指定 URL に公開される。`local1` では `build/repo1` ディレクトリにモジュールが出力される。`remote1` では指定の URL に HTTP の PUT メソッドでモジュールが転送される。そのためリポジトリが Artifactory などリポジトリ管理ツールで運用されている必要がある。

ちなみにタスク追加の際、Maven Publish プラグインは `maven()` で設定された Maven リポジトリのみを、Ivy Publish プラグインは `ivy()` で設定された Ivy リポジトリのみを参照するため、フラットディレクトリリポジトリを定義しても、使用されることはない。

認証情報は `credentials` ブロックで設定できる。

#### SFTP でモジュールをアップロードする

Maven Publish プラグインは `file` プロトコルと `http(s)` プロトコルにしか対応していないので、`WebDAV` や `SFTP` で運用しているリポジトリは利用できない。ただし、Ivy Publish プラグインに関しては Gradle 2.0 以降であれば、加えて `SFTP` でもモジュールを転送できる。`SFTP` でモジュールを転送するには、リポジトリの URL を `sftp://...` で定義する。

```gradle
apply plugin: "java"
apply plugin: "ivy-publish" // Ivy Publishプラグインを適用

//..

group = "com.example"
version = 0.1

repositories {
  mavenCentral()
}

dependencies {
  runtime "org.apache.commons:commons-lang3:3.3.1"
}

task sourceJar(type: Jar) {
  from sourceSets.main.allJava // ソースセットを入力ファイルに設定
}

publishing { // 公開設定
  publications {
    mod1(IvyPublication) { // <mod1>は任意のモジュール識別名
      from components.java // ソフトウェアコンポーネント「java」を公開対象に指定する
      // artifact()メソッドで公開するアーカイブやファイルを指定する
      artifact sourceJar {
        classifier "sources"
      }
    }
  }

  repositories {
    ivy {
      credentials {
        username 'my-name'
        password 'xxxxxx'
      }
      url "sftp://localhost:22/tmp/repo1" // sftpスキームでリポジトリを定義
    }
  }
}
```

なお、公開鍵認証にはまだ対応していない。

## Bintray / JCenter Maven リポジトリへの公開

### Bintray でリポジトリを作成する

Bintray 上のリポジトリは、**パッケージ**という単位で分割されている。ただし、Bintray を Maven リポジトリとして使うときにはパッケージは意識しない。

一方で、Bintray へモジュールをアップロードするにはまず Bintray でモジュールをホストするリポジトリとパッケージを作成する必要がある。

### Bintray のパッケージを作成する

パッケージには名前とライセンスを記入する。

### Gradle Bintray プラグイン

作成したパッケージにモジュールをアップロードするには、Bintray がメンテナンスしている Gradle プラグイン、Bintray プラグインを使用する。

```gradle
buildscript {
  repositories {
    jcenter()
 }
 dependencies {
   // bintrayプラグインを利用可能にする
   classpath "com.jfrog.bintray.gradle:gradle-bintray-plugin:0.5"
 }
}

apply plugin: "com.jfrog.bintray" // bintrayプラグインを適用

apply plugin: "java"
apply plugin: "maven-publish"

group = "com.example"
version = 0.1

repositories {
  mavenCentral()
}

dependencies {
  compile "org.apache.commons:commons-lang3:3.3.1"
}

task sourceJar(type: Jar) {
  from sourceSets.main.allJava // ソースセットを入力ファイルに設定
}

publishing {
  publications {
    mod1(MavenPublication) { // モジュール定義
      artifactId "my-bintray-module"

      from components.java // ソフトウェアコンポーネント「java」をモジュールに指定する
      // artifact()メソッドで公開するアーカイブやファイルを指定する
      artifact sourceJar {
        classifier "sources"
      }
    }
  }
}

bintray {
  user = project.has("bintrayUser") ? bintrayUser : "" // Bintray アカウント名
  key = project.has("bintrayKey") ? bintrayKey : "" // Bintray APIキー
  publications = ["mod1"] // Bintrayへアップロードするモジュール
  publish = true
  pkg {  // アップロード先のBintrayパッケージ情報
    repo = "test1"
    name = "my-pkg"
  }
}
```

Bintray プラグインの設定を行う `bintray` ブロック内で、`publications` プロパティに Maven Publish プラグインの `MavenPublication` で宣言したモジュールのうち Bintray にアップロードしたいものを指定する。`pkg` ブロックにアップロード先の Bintray パッケージ情報を設定する。`user`, `key` には Bintray の認証情報を設定する。

セキュリティの観点から、認証情報はビルドスクリプトには直接書き込まず、プロパティから読み出すようにして、`~/.gradle/gradle.properties` や環境変数などから取得するようにしたほうが良い。

### アップロードしたモジュールの公開

Bintray サイト上で Publish ボタンを押せば公開される。ビルドスクリプトの `bintray` ブロックで `publish` プロパティに `true` を設定することでアップロード後に自動で公開することもできる。

### アップロードしたモジュールを使用する

`http://dl.bintray.com/<account>/<repository>` でアクセスできるようになるので、ビルドスクリプトのリポジトリ定義にこれを指定する。

```gradle
apply plugin: "application"
mainClassName = "com.example.Client"

apply plugin: "java"

repositories {
  // com.example:my-bintray-module:0.1の依存ライブラリを取得する先
  jcenter()

  // com.example:my-bintray-module:0.1を取得する先
  maven {
    // http://dl.bintray.com/<アカウント名>/<リポジトリ名>
    url "http://dl.bintray.com/gradle-book/test1"
  }
}

dependencies {
  compile "com.example:my-bintray-module:0.1"
}
```

#### 別パッケージに同じモジュールはアップロードできない

Bintray 上の別パッケージに同じモジュールをアップロードしようとしても、409 Conflict とエラーが返却される。モジュールの利用側では Bintray 上のパッケージは意識しないため、モジュールが一意である必要がある。

### JCenter Maven リポジトリへの公開

JCenter は、Bintray 上の Maven Central リポジトリと言って良い。このリポジトリにアップロードされているモジュールは `jcenter()` を呼び出すだけで利用できる。モジュールの利用側がビルドスクリプトの中でリポジトリを多数定義するのは煩雑なので、できれば JCenter や Maven Central リポジトリにモジュールを公開するのが良い。

JCenter に公開するためには、Bintray 上のリポジトリを JCenter リポジトリからリンクしてもらう形式を取る。Bintray 上の管理 UI から Add to JCenter ボタンを押して申請することでリンクできる。申請にはコメントが必要なので記入して申請ボタンを押す。数日後には公開されているはず。

## Maven Central リポジトリへの公開

Maven Publish プラグインは Meven Central リポジトリへの公開には対応しておらず、方法としては Bintray と Maven Publish プラグインを組み合わせて行う方法がある。

### 公開できるモジュールの要件

* モジュールの POM に次の要素が設定されていること
    * `<modelVersion>`
    * `<groupId>`
    * `<artifactId>`
    * `<version>`
    * `<packaging>`
    * `<name>`
    * `<description>`
    * `<url>`
    * `<licenses>`
    * `<scm>`
        * `<url>`
    * `<scm>`
        * `<connection>`
    * `<developers>`
* モジュールにソースコード JAR が含まれていること
    * ソースコード JAR のファイル名は `<artifactId>-<version>-sources.jar` であること
* モジュールに Javadoc JAR が含まれていること
    * Javadoc JAR のファイル名は `<artifactId>-<version>-javadoc.jar` であること
* モジュールに含まれる全てのアーティファクトが PGP 署名されていること。
    * その署名の公開鍵が `hkp://pool.sks-keyservers.net/` で配布されていること

最後の PGP 署名に関する要件以外は、Maven Publish プラグインの機能でクリアできる。

### Maven Central リポジトリへの公開手順

Maven Publish プラグインはアーティファクトの署名に対応していないので、前項の最後の要件が満たせない。ただし、Bintray が以下の機能を持っているのでこれを利用して Maven Central リポジトリへモジュールをアップロードできる。

* Bintray パッケージ上にアップロードされたモジュールのファイル全てに署名する
* JCenter へリンクしたモジュールを Maven Central リポジトリへ同期する

### 公開前の準備作業

#### POM の整備

```gradle
publishing {
  publications {
    mod1(MavenPublication) {
      from components.java

      final customPom = { // Mavenセントラルリポジトリが要求するPOM
        packaging "jar"
        name "Gradle XXX Plugin"
        url "https://example.com/module-site/xxx"
        description "This is a sample module for mvn central."
        licenses {
          license {
            name "The Apache Software License, Version 2.0"
            url "http://www.apache.org/licenses/LICENSE-2.0.txt"
            distribution "repo"
          }
        }
        scm {
          url "https://github.com/xxx/xxx"
          connection "https://github.com/xxx/xxx"
        }
        developers {
          developer {
            id "my-name-id"
            name "Taro Gradle"
            email "xxx@example.com"
          }
        }
      }
      customPom.resolveStrategy = Closure.DELEGATE_FIRST

      pom.withXml { xml ->
        final root = xml.asNode()
        root.children().last() + customPom
      }
    }
  }
}
```

#### ソースコード JAR、Javadoc JAR の添付

```gradle
task sourcesJar(type: Jar) { // ソースコードJARの作成タスク
  from sourceSets.main.allJava // ソースセットを入力ファイルに設定
  classifier = "sources"
}

task javadocJar(type: Jar, dependsOn: javadoc) { // Javadoc JARの作成タスク
  classifier 'javadoc'
  from javadoc.destinationDir // Javadocのターゲットディレクトリを入力ファイルに設定
}

jar.dependsOn sourcesJar, javadocJar // jarタスク実行時にソースコードJARとJavadoc JARも作成する

publishing {
  publications {
    mod1(MavenPublication) {
      from components.java
      artifact sourcesJar // ソースコードJARをモジュールに含める
      artifact javadocJar // Javadoc JARをモジュールに含める
```

ここまでで、署名を除きモジュールの準備が完了した。

#### 署名に使う鍵ペアを用意する

モジュールの署名は、Bintray にアップロードした後、Bintray に実行してもらう。そのためまず署名に使う鍵を作成し、Bintray に送信する必要がある。また、署名の公開鍵をキーサーバーにアップロードして他システムが参照できるようにする。

署名に使う鍵ペアは、GnuPG（GPG）で作成できる。`gpg` コマンドがインストールされていない場合、[GnuPG のサイト](https://www.gnupg.org/download/)からダウンロード、インストールできる。

```console
$ gpg --version
```

#### GPG による鍵の作成

```console
$ gpg --gen-key
```

途中で鍵の種類や鍵長、有効期限などいろいろ聞かれるが全てデフォルトで構わない。`Real name`、`Email address`、`Comment` はモジュールのユーザーが署名を確認する際に参考にすると思われるのでわかりやすいものを入力する。最後にパスフレーズを入力して鍵の作成は終了。

正しく鍵が作成できたかどうかは、以下のコマンドで確認できる。

```shell-session
$ gpg --list-keys  # 鍵束に追加された公開鍵の一覧を表示
/Users/xxx/.gnupg/pubring.gpg  # 鍵束ファイルの場所
```

#### 鍵ペアを Bintray へ送信する

Bintray に鍵ペアを保存しておくと、その鍵でアップロードしたパッケージ内のモジュールに署名できるようになる。鍵を Bintray に送信するには、Bintray のアカウント情報編集ページで、先ほど作成した鍵を入力する。

ここで入力する鍵は *ASCII Armored* と呼ばれる文字列形式の鍵データ。次のコマンドで取得できる。

```console
# 秘密鍵の表示
$ gpg -a --export-secret-key EF401D1D # EF401D1D は鍵の ID

# 公開鍵の表示
$ gpg -a --export EF401D1D # EF401D1D は鍵の ID
```

#### 公開鍵をキーサーバーへ送信する

Maven Central リポジトリやモジュールのユーザーが署名を検証できるようにするため、署名の公開鍵をキーサーバーへアップロードしておく必要がある。Maven Central リポジトリが指定するキーサーバーは次の通り。

    hkp://pool.sks-keyservers.net

次のコマンドで、作成した公開鍵をキーサーバーへアップロードする。

```console
$ gpg --keyserver hkp://pool.sks-keyservers.net --send-keys EF401D1D
```

`--keyserver` オプションにキーサーバーの場所を、`--send-keys` オプションに送信したい鍵の ID を指定する。

これで、Maven Central リポジトリへアップロードするモジュールと署名に使う鍵ペアの準備が完了した。

### Bintray へモジュールをアップロードする

```gradle
buildscript {
  repositories {
    jcenter()
  }
  dependencies {
    classpath 'com.jfrog.bintray.gradle:gradle-bintray-plugin:0.5'
  }
}

bintray {
  user = bintrayUser
  key = bintrayKey
  publications = ["mod1"]
  pkg {
    repo = "test1"
    name = "my-pkg-2"
  }
}
```

```console
$ gradle bintrayUpload
```

### モジュールに署名する

Bintray でモジュールに署名するには、Bintray サイト上のリポジトリ設定画面で署名機能を有効にする。Sign this repository's files with key from: ... にチェックを入れる。

リポジトリの操作やパッケージの作成など、Bintray サイト上で行うこともできるが、殆どの操作を REST API を通して行うこともできる。モジュールの署名は以下の API で実行できる。

```
POST /gpg/<account>/<repository>/<package>/versions/<署名するバージョン>
{
  "passphrase": "<アップロードした鍵のパスフレーズ>"
}
```

Gradle には HTTP クライアントが組み込まれているので、次のようなタスクを作れば上記の REST API を叩いてモジュールに署名できる。

```gradle
// BintrayのREST APIを叩いてアップロードしたモジュールに署名するタスク
task signBintrayPackage << {
  final http = new HTTPBuilder(bintray.apiUrl) // 「bintray」はBintrayプラグインにより設定されるプロパティ

  // BASIC認証
  http.auth.basic bintrayUser, bintrayKey

  // BintrayのREST APIに対するリクエスト
  http.request(POST, JSON) {
    uri.path = "/gpg/${bintrayUser}/${bintray.pkg.repo}/${bintray.pkg.name}/versions/${project.version}"
    body = [passphrase: signKeyPassphrase]
    response.success = { resp ->
      logger.info("Signed version ${project.version}.")
    }
    response.failure = { resp ->
      throw new GradleException("Could not sign version ${project.version}: $resp.statusLine")
    }
  }
}
```

この `signBintrayPackage` タスクを実行すると、先ほど Bintray にアップロードしたファイルに署名することができる。Bintray サイト上でパッケージの詳細画面を開くと、`*.asc` のファイルが登録されているはず。これが署名ファイル。

### JCenter へ公開する

これで Maven Central リポジトリへ公開するモジュールの準備は署名も含めて全て完了した。Bintray から Maven Central リポジトリへモジュールを送信するには、まず JCenter へモジュールを公開する必要がある。

### Maven Central リポジトリへ公開する

#### 公開申請

JCenter から Maven Central リポジトリへモジュールをアップロードするには、アップロードしたいモジュールの `groupId` に対して Sonatype OSS のアップロード権限が必要。権限の申請は、*issues.sonatype.org* に所定の形式で JIRA チケットを作成して行う。承認までの期間はだいたい1〜2営業日ほど。

#### モジュールを JCenter から Maven Central リポジトリへ転送する

Maven Central リポジトリへのアップロードが許可されれば、JCenter から Maven Central リポジトリへモジュールを転送できる。Bintray のパッケージ画面を開き、Maven Central を選択すると、認証情報を入力するページが表示されるので、Sonatype OSS の username と password を入力して Sync ボタンを押す。
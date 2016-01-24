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


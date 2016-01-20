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


#!/bin/sh

if [ "$1" = "" ]; then
	echo "输入 -h 查看使用说明"
    exit 0
fi

if [ "$1" = "-h" ]; then
    echo "使用说明："
    echo "执行 sh $0 [apkName]"
    echo "apkName为你当前项目的apk名字前缀即可" 
    exit 0
fi

#最好是写自己app的前缀
appName="$1"

./gradlew clean assembleRelease

# 获取当前达到的包,应为安装命名规范来,所以最新的达到的包是在最后面
for apkFile in `ls -d app/build/outputs/apk/${appName}*`;
do
echo $apkFile
done

# 获取Apk的名字 
echo ${apkFile}
apkName=${apkFile%%_*}
apkName=${apkName##*/}
echo "apkName = "${apkName}

# 更名名称解析版本号
version=${apkFile#*_}
version=${version%%_*}
echo "version = "${version}

# 获取mapping对应的文件名
cpMappingName=${apkFile##*/}
cpMappingName=${cpMappingName%.*}
echo "cpMappingName = "${cpMappingName}

# 这里写死了 mapping文件的路径
mapping="app/build/outputs/mapping/release/mapping.txt"
echo "mapping = "${mapping}

# 备份apk mapping文件的地址
cpDir="release/mapping"
cpApkDir="release/apk"

# 创建mapping备份地址
if [ -d ${cpDir} ]
then
echo have dir
else
  mkdir -p ${cpDir}
fi

# 创建apk备份地址
if [ -d ${cpApkDir} ]
then
echo have dir
else
  mkdir -p ${cpApkDir}
fi

# 复制 apk 以及mapping文件
echo "cpApkDir = "${cpApkDir}
cp ${apkFile} ${cpApkDir}
echo "cpDir = "${cpDir}
cp ${mapping} ${cpDir}"/"${cpMappingName}"_"mapping.txt

# 打开finder 方便把apk 发给测试
#cd app/build/outputs/apk/
#open .

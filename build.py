from pathlib import Path
import os,subprocess,zipfile,shutil
root=Path(__file__).resolve().parent
sdk=Path(os.environ.get('EV_SDK',str(root.parent/'build-tools-ev'))).resolve()
build=root/'build04';classes=build/'classes'
if classes.exists():shutil.rmtree(classes)
classes.mkdir(parents=True)
def run(*a):subprocess.run([str(x) for x in a],check=True,cwd=root)
run('java','-jar',sdk/'ecj.jar','-1.8','-bootclasspath',str(sdk/'android-35/android.jar')+':'+str(sdk/'android-15/core-lambda-stubs.jar'),'-d',classes,*sorted((root/'src').rglob('*.java')))
source=root/'EV-Range-0.5.2-source.zip'
with zipfile.ZipFile(source,'w',zipfile.ZIP_DEFLATED) as z:
 for f in [root/'README.md',root/'AndroidManifest.xml',root/'build.py',*root.joinpath('src').rglob('*.java'),*root.joinpath('tests').rglob('*.java'),*[f for f in root.joinpath('res').rglob('*') if f.is_file()]]:z.write(f,f.relative_to(root))
with zipfile.ZipFile(build/'classes.jar','w') as z:
 for f in classes.rglob('*.class'):z.write(f,f.relative_to(classes))
for name in ['d8','aapt2','zipalign','apksigner','aapt']:(sdk/'android-15'/name).chmod(0o755)
run(sdk/'android-15/d8','--release','--min-api','26','--lib',sdk/'android-35/android.jar','--output',build,build/'classes.jar')
run(sdk/'android-15/aapt2','compile','--dir',root/'res','-o',build/'resources.zip')
run(sdk/'android-15/aapt2','link','-o',build/'base.apk','--manifest',root/'AndroidManifest.xml','-I',sdk/'android-35/android.jar',build/'resources.zip')
shutil.copyfile(build/'base.apk',build/'unsigned.apk')
with zipfile.ZipFile(build/'unsigned.apk','a') as z:
 z.write(build/'classes.dex','classes.dex');z.write(source,'assets/source.zip')
run(sdk/'android-15/zipalign','-f','-p','4',build/'unsigned.apk',build/'aligned.apk')
run(sdk/'android-15/apksigner','sign','--ks',root/'private-signing/release.p12','--ks-pass','file:'+str(root/'private-signing/password.txt'),'--out',root/'EV-Range-0.5.2.apk',build/'aligned.apk')
run(sdk/'android-15/apksigner','verify','--verbose',root/'EV-Range-0.5.2.apk')

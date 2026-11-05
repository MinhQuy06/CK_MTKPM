#!/usr/bin/env python3
"""Run each browser test against a new, disposable H2 fixture (JDK21/Maven/Chrome)."""
from pathlib import Path
import os, subprocess, tempfile, time, urllib.request
root=Path(__file__).resolve().parents[1]
port=os.environ.get('CK_BROWSER_PORT','18082')
origin='http://localhost:'+port
try:
    urllib.request.urlopen(origin,timeout=1)
except Exception as e:
    # HTTP errors still mean another server owns the port.
    if isinstance(e, __import__('urllib.error',fromlist=['HTTPError']).HTTPError):
        raise SystemExit('Port occupied; set CK_BROWSER_PORT to a free port.')
else:
    raise SystemExit('Port occupied; set CK_BROWSER_PORT to a free port.')
with tempfile.TemporaryDirectory(prefix='ck-browser-') as temp:
    cpfile=Path(temp)/'classpath.txt'
    subprocess.run(['bash','scripts/maven.sh','-q','-f','backend/pom.xml','test-compile',
        'dependency:build-classpath','-Dmdep.outputFile='+str(cpfile)],cwd=root,check=True)
    cp='backend/target/classes:backend/target/test-classes:'+cpfile.read_text().strip()
    names=__import__('sys').argv[1:] or [p.name for p in sorted((root/'frontend/tests').glob('browser-*.cjs'))]
    for name in names:
        if Path(name).name!=name or not name.startswith('browser-') or not name.endswith('.cjs'):
            raise SystemExit('Expected browser-*.cjs basename: '+name)
        log=Path(temp)/'server.log'
        args=['java','-Dspring.devtools.restart.enabled=false']
        if name=='browser-precision-booking.cjs':args+=['-Dapp.pricing.insurance-discount-rate=0.123456789123456789']
        with log.open('w') as output:
            server=subprocess.Popen(args+['-cp',cp,'com.datlichkhambenh.backend.ConsultationQuoteBrowserServer',port],cwd=root,stdout=output,stderr=subprocess.STDOUT)
            try:
                for _ in range(600):
                    content=log.read_text()
                    if 'QUOTE_BROWSER_FIXTURE_READY' in content: break
                    if server.poll() is not None:raise RuntimeError(content[-4000:])
                    time.sleep(.1)
                else:raise RuntimeError('Fixture startup timeout: '+log.read_text()[-2000:])
                print('RUN',name,flush=True)
                subprocess.run(['node','frontend/tests/'+name],cwd=root,env={**os.environ,'QUOTE_TEST_ORIGIN':origin},check=True)
            finally:
                server.terminate()
                try:server.wait(timeout=10)
                except subprocess.TimeoutExpired:server.kill();server.wait()
print('PASS: isolated browser suite; each fixture stopped after test.',flush=True)

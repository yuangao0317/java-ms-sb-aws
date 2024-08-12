# java-ms-sb-aws

# Setup with multi-version supoort (aws-cli, aws-cdk-cli, NodeJs, JDK, Maven)
- Setup different Java version in IntelliJ IDEA:  
search 'JDK', then download or select one.  

- Create or Edit the **~/.aws/credentials** File:  
The credentials file is typically located at ~/.aws/credentials on Unix-based systems (Linux, macOS) and C:\Users\USERNAME\.aws\credentials on Windows.  
Example ~/.aws/credentials File:
```
[default]
aws_access_key_id = YOUR_ACCESS_KEY_ID
aws_secret_access_key = YOUR_SECRET_ACCESS_KEY

[my-profile]
aws_access_key_id = ANOTHER_ACCESS_KEY_ID
aws_secret_access_key = ANOTHER_SECRET_ACCESS_KEY
```

- Install cdk-cli in different Node version (what if several projects use same Node but different cdk-cli versions):  
```
nvm use 18.14.0
```

- Install cdk-cli locally in project instead of installing and managing several version on local machine:  
```
npm install aws-cdk@2.114.1 --save-dev  
npx aws-cdk@2.114.1 --version
```

```
PS C:\java-sb3-aws-sdk-cdk-microservices> cd .\java-ms-sb-aws\
PS C:\java-sb3-aws-sdk-cdk-microservices\java-ms-sb-aws> npm install aws-cdk@2.114.1 --save-dev

added 1 package in 2s
PS C:\java-sb3-aws-sdk-cdk-microservices\java-ms-sb-aws> cd cdk
PS C:\java-sb3-aws-sdk-cdk-microservices\java-ms-sb-aws\cdk> npx aws-cdk@2.114.1 --version
2.114.1 (build 02bbb1d)
PS C:\java-sb3-aws-sdk-cdk-microservices\java-ms-sb-aws\cdk> npx aws-cdk@2.114.1 init app --language java
```
- Use Maven from IntelliJ instead of intalling locally:
IntelliJ's maven bundle won't work, you need to use its D:\Program Files\JetBrains\IntelliJ IDEA Community Edition 2024.1.4\plugins\maven\lib\maven3  
And set system environment variable M2_HOME=D:\Program Files\JetBrains\IntelliJ IDEA Community Edition 2024.1.4\plugins\maven\lib\maven3  
And then add %M2_HOME%\bin to Path varibale in system environment.  

- Use multi-version aws-cli through Docker without installing and managing them on your local machine:  
```
docker run --rm -it amazon/aws-cli:latest --version  
docker run --rm -it amazon/aws-cli:2.9.22 --version  
docker run --rm -it \
  -e AWS_ACCESS_KEY_ID=<your-access-key-id> \
  -e AWS_SECRET_ACCESS_KEY=<your-secret-access-key> \
  -e AWS_DEFAULT_REGION=<your-region> \
  amazon/aws-cli:2.9.22 --version  
```


# Used Commands
```
docker build --no-cache --progress=plain -f Dockerfile.full -t product-service-full:1.0.0 .
```

```
npx aws-cdk@2.114.1 init app --language=java
```

Execute bootstrap only once per region per account
```
npx aws-cdk@2.114.1 bootstrap --profile default
```
```
npx aws-cdk@2.114.1 deploy ECR-Stack --profile default
npx aws-cdk@2.114.1 deploy --all --require-approval never
npx aws-cdk@2.114.1 destroy ECR-Stack --profile default
npx aws-cdk@2.114.1 destroy --all --profile default
```
```
npx aws-cdk@2.114.1 deploy ECR-Stack --profile default
```
```
npx aws-cdk@2.114.1 diff
```

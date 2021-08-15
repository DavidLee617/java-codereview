#include <bits/stdc++.h>
using namespace std;
int main()
{
	float wei,m;
	cin>>wei;
	if(wei>30)  cout<<"Fail"<<endl;
	else if(wei>20) m=(wei-20)*0.70+15.7;
	else if((wei>10)&&(wei<=20)) m=(wei-10)*0.75+8.2;
	else if(wei<10) m=0.8*wei+0.2 ;
	cout<<fixed<<setprecision(2)<<m;
	return 0;
}
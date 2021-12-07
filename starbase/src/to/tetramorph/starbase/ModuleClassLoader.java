/*
 * ModuleClassLoader.java
 *
 * Created on 2007/12/07, 5:15
 *
 */

package to.tetramorph.starbase;

import java.net.URL;
import java.net.URLClassLoader;
import java.security.AccessControlContext;
import java.security.AccessController;
import java.security.AllPermission;
import java.security.CodeSource;
import java.security.PermissionCollection;
import java.security.Permission;

/**
 * プラグインモジュールをロードするための専用のクラスローダ。
 * @author 大澤義鷹
 */
class ModuleClassLoader extends URLClassLoader {
    AccessControlContext acc;
    Permission [] permissions;
    /**  
     * ModuleClassLoader オブジェクトを作成する 
     */
    public ModuleClassLoader( URL [] urls ) {
        super(urls, ModuleClassLoader.class.getClassLoader() );
        acc = AccessController.getContext();
    }

    protected PermissionCollection getPermissions(CodeSource codesource) {
        PermissionCollection perms = super.getPermissions(codesource);
        for ( Permission perm : permissions )
            perms.add( perm );
        return perms;
    }
    /**
     * モジュールをロードする。ChartPane,SearchDialogがこのメソッドを使って、
     * プラグイン(チャート/検索モジュール)をロードする。権限を表すインスタンスは
     * それぞれのクラスの中で作成して、このメソッドに引き渡している。
     * @param name ロードするクラスのバイナリ名
     * @param permissions ロードするクラスに付与する権限
     */
    public Class<?> loadModule(String name,Permission [] permissions) 
                                                throws ClassNotFoundException {
        this.permissions = permissions;
        return super.loadClass(name);
    }
    /**
     * モジュールをロードする。モジュールに全権を与えてロードする。これはテスト用
     * のメソッドで本番では決して使用してはいけない。適切な権限を与えてロードす
     * ること。
     */
    public Class<?> loadModule(String name) throws ClassNotFoundException {
        permissions = new Permission[] {
            new AllPermission()
        };
        return super.loadClass(name);
    }
    
}

package io.github.oppsgo.android.theme.binding;

import android.view.ViewGroup;

import androidx.annotation.NonNull;

/** 默认的 {@link ViewGroup} 绑定，刷新子树的逻辑在 {@link BaseViewGroupResourceBinding}。 */
public class ViewGroupResourceBinding extends BaseViewGroupResourceBinding {

    public ViewGroupResourceBinding(@NonNull ViewGroup view) {
        super(view);
    }

    /** 已有本类或子类就返回；否则给一个不挂到 View 上的实例。 */
    @NonNull
    public static ViewGroupResourceBinding of(@NonNull ViewGroup view) {
        return of(view, ViewGroupResourceBinding.class, ViewGroupResourceBinding::new);
    }
}

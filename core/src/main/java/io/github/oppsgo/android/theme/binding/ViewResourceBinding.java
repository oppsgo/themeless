package io.github.oppsgo.android.theme.binding;

import android.view.View;

import androidx.annotation.NonNull;

/**
 * {@link View} 的默认绑定。background / backgroundTint 的 setter 在
 * {@link BaseViewResourceBinding}，以便 ViewGroup Binding 也能直接调用。
 */
public class ViewResourceBinding extends BaseViewResourceBinding<View> {

    public ViewResourceBinding(@NonNull View view) {
        super(view);
    }

    /** 已有本类或子类就返回；否则给一个不挂到 View 上的实例。 */
    @NonNull
    public static ViewResourceBinding of(@NonNull View view) {
        return of(view, ViewResourceBinding.class, ViewResourceBinding::new);
    }
}
